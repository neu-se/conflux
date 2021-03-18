import json
import os


def main():
    dir_name = os.path.dirname(__file__)
    report_file = os.path.join(dir_name, 'conflux-experiments', 'experiments-report', 'target', 'flow-studies.json')
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
    main()
