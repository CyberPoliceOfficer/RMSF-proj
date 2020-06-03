from configparser import ConfigParser

# Rotina que carrega configurações
def config(filename='config.ini', section='main'):
    # Criar o parser
    parser = ConfigParser()
    # Criar a config file
    parser.read(filename)

    # get section
    db = {}
    if parser.has_section(section):
        params = parser.items(section)
        for param in params:
            db[param[0]] = param[1]
    else:
        raise Exception('Section {0} not found in the {1} file'.format(section, filename))

    return db

# Rotina que escreve novos parametros no ficheiro de configurações
def set_setting(filename, section, parameter, value):
    # Criar o parser
    parser = ConfigParser()
    # Criar a config file
    parser.read(filename)

    parser.set(section, parameter, value)

    fp = open(filename, 'w')
    parser.write(fp, space_around_delimiters=True)
