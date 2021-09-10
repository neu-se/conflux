import json
import os


def per_line_counts():
    dir_name = os.path.dirname(__file__)
    report_file = os.path.join(dir_name, '../conflux-experiments', 'experiments-report', 'target', 'flow-studies.json')
    with open(report_file) as f:
        report_data = json.load(f)
    names = ['total', 'data-only', 'basic-control', 'strict-control', 'conflux']
    for study in report_data['studies']:
        values = study[1]['input']
        configuration_predictions_map = study[1]['configurationPredictionsMap']
        project = study[0]['project']
        print("\n" + project)
        current_line_counts = {name: 0 for name in names}
        row = 0
        for i in range(0, len(values)):
            value = values[i]
            current_line_counts['total'] += 1
            for policy in configuration_predictions_map:
                if configuration_predictions_map[policy][i]:
                    current_line_counts[policy] += 1
            if value == '\n' or (project == 'openrefine' and (i+1) < len(values) and values[i+1] == '{'):
                row = print_line_counts(current_line_counts, row)
                current_line_counts = {name: 0 for name in names}
        print_line_counts(current_line_counts, row)


def print_line_counts(current_line_counts, row):
    policies = ['data-only', 'basic-control', 'strict-control', 'conflux']
    total = current_line_counts['total']
    if total > 0:
        row += 1
        values = [f"{current_line_counts[policy] / total:.1f}" for policy in policies]
        print(f" & {' & '.join(values)} \\\\ % {row}")
    return row


def counts():
    dir_name = os.path.dirname(__file__)
    report_file = os.path.join(dir_name, '../conflux-experiments', 'experiments-report', 'target', 'flow-studies.json')
    with open(report_file) as f:
        report_data = json.load(f)
    studies = report_data['studies']
    names = ['total', 'data-only', 'basic-control', 'strict-control', 'conflux']
    projects = ['checkstyle', 'closure', 'rhino', 'openrefine', 'h2']
    results = {}
    for study in studies:
        values = study[1]['input']
        configuration_predictions_map = study[1]['configurationPredictionsMap']
        project = study[0]['project']
        temp = {policy: sum(configuration_predictions_map[policy]) for policy in configuration_predictions_map}
        temp['total'] = len(values)
        results[project] = temp
    for project in projects:
        values = [str(results[project][name]) for name in names]
        print(f"{project.title()} & {' & '.join(values)} \\\\")


if __name__ == "__main__":
    per_line_counts()
