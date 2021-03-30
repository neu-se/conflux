import os


def main():
    dir_name = os.path.dirname(__file__)
    plot_dir = os.path.join(dir_name, 'conflux-experiments', 'experiments-report', 'target', 'flow-plots')
    plot_files = [os.path.join(plot_dir, f) for f in os.listdir(plot_dir)]
    categories = [categorize(read_lines(plot_file)) for plot_file in plot_files]
    print(len(categories))
    print(frequencies(categories))


def read_lines(file_path):
    with open(file_path) as f:
        return f.readlines()


def frequencies(values):
    result = {}
    for value in values:
        if value in result:
            result[value] = result[value] + 1
        else:
            result[value] = 1
    return result


def categorize(plot):
    policies = ['data-only', 'basic-control', 'scd', 'conflux']
    lines = [line.strip() for line in plot]
    values = [float(line[line.index(',') + 1:line.index(')')]) for line in lines if
              line.startswith('(8,') or line.startswith('(1024,')]
    i = 0
    result = ""
    for policy in policies:
        v1 = values[i]
        v2 = values[i + 1]
        i += 2
        diff = v2 - v1
        if diff == 0:
            result += F'{policy}-constant '
        elif diff > 0:
            result += F'{policy}-increase '
        elif v2 < 0.05:
            result += F'{policy}-zero '
        else:
            result += F'{policy}-non-zero '
    return result


if __name__ == "__main__":
    main()
