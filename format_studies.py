import json
import sys


def main():
    report_file = sys.argv[1]
    with open(report_file) as f:
        report_data = json.load(f)
    studies = report_data['studies']
    configuration_names = ['data-only', 'basic-control', 'strict-control', 'conflux']
    for study in studies:
        values = study[1]['input']
        configuration_predictions_map = study[1]['configurationPredictionsMap']
        study_name = f"{study[0]['project']}-{study[0]['issue']}"
        result = format_study(study_name, values, configuration_predictions_map, configuration_names)
        print(f"\n\n{study_name}\n")
        print(result)


def format_study(study_name, values, configuration_predictions_map, configuration_names):
    marked = []
    commands = []
    result = [F"\\begin{{lstlisting}}[caption={{{study_name}}}, style=study]\n"]
    for i in range(0, len(configuration_names)):
        predictions = configuration_predictions_map[configuration_names[i]]
        t_marked, t_commands = calculate_underlines(values, predictions, i)
        marked.extend(t_marked)
        commands.extend(t_commands)
    marked = set(marked)
    for i in range(0, len(values)):
        if i in marked:
            result.append(mark_position(i))
        result.append(values[i])
    result.append(mark_position("END"))
    result.append("\n\\end{lstlisting}\n")
    result.append("\n".join(commands))
    return ''.join(result)


def calculate_underlines(values, predictions, configuration_index):
    start = -1
    marked = []
    commands = []
    for i in range(0, len(values)):
        value = values[i]
        prediction = predictions[i]
        if value == '\n':
            if start != -1:
                # Stop line before i
                marked.append(i)
                commands.append(format_command(configuration_index, start, i))
                start = -1
            continue
        if prediction and start == -1:
            # Start line before i
            start = i
            marked.append(i)
        if not prediction and start != -1:
            # Stop line before i
            marked.append(i)
            commands.append(format_command(configuration_index, start, i))
            start = -1
    if start != -1:
        commands.append(format_command(configuration_index, start, 'END'))
    return marked, commands


def format_command(configuration_index, start, end):
    commands = ['underlineA', 'underlineB', 'underlineC', 'underlineD']
    return f"\\{commands[configuration_index]}{{{start}}}{{{end}}}"


def mark_position(name):
    return f"!@\\tikzmark{{{name}}}@!"


if __name__ == "__main__":
    main()
