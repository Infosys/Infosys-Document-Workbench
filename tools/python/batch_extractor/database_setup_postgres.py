# ===============================================================================================================#
# Copyright 2024 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import argparse
import os
import sys
import subprocess
import platform

VERSION = '0.0.1'


class Color():
    """Color class for text color"""
    DEFAULT = '\033[0m'
    YELLOW = '\033[93m'
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    GRAY = '\033[90m'


INPUT_DATA_DICT = {
    "action": None,
    "sql_dir_path": None,
    "sql_file_path": None
}


def __parse_input():
    input_data_dict = INPUT_DATA_DICT.copy()
    parser = argparse.ArgumentParser()
    parser.add_argument("--action", default=None,
                        required=True, choices=['newdb', 'newuser'])
    parser.add_argument("--sql_dir_path", default=None, required=False)
    parser.add_argument("--sql_file_path", default=None, required=False)
    args = parser.parse_args()
    input_data_dict['action'] = args.action
    input_data_dict['sql_dir_path'] = args.sql_dir_path
    input_data_dict['sql_file_path'] = args.sql_file_path
    return input_data_dict


def __validate_prerequisites():
    """Validate prerequisites"""
    print(
        f"{Color.CYAN}Checking for Postgres executable [OS={platform.system()}{Color.DEFAULT}]")
    print("")
    if platform.system() == "Windows":
        psql_path = "C:\\Program Files\\PostgreSQL\\17\\bin"
        os.environ['PATH'] = f"{psql_path};{os.environ['PATH']}"
    else:
        psql_path = "/usr/bin/psql"
        os.environ['PATH'] = f"{psql_path}:{os.environ['PATH']}"

    # Checking for posgresql installation
    psql_executable_found = False
    try:
        result = subprocess.run(
             ["psql", "--version"], capture_output=True, text=True, check=False, env=os.environ)
        if result.stdout:
            psql_executable_found = True
            print(f"{Color.GREEN}✓ {result.stdout}{Color.DEFAULT}")
        else:
            print(f"{Color.RED}{result.stderr}{Color.DEFAULT}")
            print(
                f"{Color.RED}Postgres executable (psql) version check failed.{Color.DEFAULT}")
    except FileNotFoundError as e:
        print(f"{Color.RED}{e}{Color.DEFAULT}")
        print(
            f"{Color.RED}ERROR: Postgres executable (psql) was not found in this system{Color.DEFAULT}")
        print("")
        print(f"{Color.YELLOW}Please follow any option from below.{Color.DEFAULT}")
        print(f"{Color.YELLOW}1) Install postgres in this system OR run this setup from another system containing psql executable.{Color.DEFAULT}")
        print(f"{Color.YELLOW}2) If postgres is already installed in this system, then modify THIS script to provide the executable's path in psql_path varable.{Color.DEFAULT}")
    finally:
        if not psql_executable_found:
            print("")
            input("Press any key to exit...")
            sys.exit(1)


def __execute_command(command_list, output_file_path=None):
    """Execute command"""
    for command in command_list:
        print(f"{Color.GRAY}Executing: {command}{Color.DEFAULT}")
        if output_file_path:
            with open(output_file_path, 'a', encoding='utf-8') as output_file:
                subprocess.run(command, shell=True,
                               stdout=output_file, stderr=subprocess.STDOUT,
                               check=False,
                               env=os.environ)
        else:
            subprocess.run(command, shell=True, check=False, env=os.environ)


def __show_configuration(config_data):
    """Show configuration"""
    for key, value in config_data.items():
        _value = value
        if "password" in key.lower():
            _value = "********"
        print(f"{Color.CYAN}{key} : {_value}{Color.DEFAULT}")


def __create_database(config_data, sql_scripts_dir_path):
    """Create database"""
    os.environ['PGPASSWORD'] = config_data["postgresPassword"]
    psql_command = "psql"
    psql_command += f' -p {config_data["postgresPortNum"]} -h {config_data["postgresHostIPAddress"]}'
    psql_command += f' -U {config_data["postgresUser"]}'
    postgres_dbname = config_data["postgresDBName"]
    postgres_maintenance_dbname = config_data["postgresMaintenanceDBName"]

    ##### Step #####
    print(
        f"\n{Color.YELLOW}Checking database version{Color.DEFAULT}")
    commands = []
    commands.append(
        f'{psql_command} -d {postgres_maintenance_dbname} -c "SELECT version();" ')
    __execute_command(commands)

    ##### Step #####
    print(f"\n{Color.YELLOW}Creating database{Color.DEFAULT}")
    commands = []
    commands.append(
        f'{psql_command} -d {postgres_maintenance_dbname} -c "CREATE DATABASE {postgres_dbname} ;" ')
    __execute_command(commands)

    ##### Step #####
    print(f"\n{Color.YELLOW}Creating extensions{Color.DEFAULT}")
    commands = []
    commands.append(
        rf'{psql_command} -d {postgres_dbname} -c "SET search_path TO public; CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";" ')
    commands.append(
        f'{psql_command} -d {postgres_dbname}  -c "SET search_path TO public; SELECT uuid_generate_v4();" ')
    __execute_command(commands)

    ##### Step #####
    print(f"\n{Color.YELLOW}Creating roles{Color.DEFAULT}")
    commands = []
    commands.append(
        f'{psql_command} -d {postgres_maintenance_dbname} -f "{sql_scripts_dir_path}/docwbmain/dbRoleSetup.sql" -v postgresDBName={postgres_dbname}')
    __execute_command(commands)

    ##### Step #####
    print(f"\n{Color.YELLOW}Creating schema{Color.DEFAULT}")
    commands = []
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_scripts_dir_path}/docwbmain/dbSchemaSetup.sql"')
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_scripts_dir_path}/workflow/dbSchemaSetup.sql"')
    __execute_command(commands)

    ##### Step #####
    print(f"\n{Color.YELLOW}Inserting records{Color.DEFAULT}")
    commands = []
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_scripts_dir_path}/docwbmain/dbDataSetup.sql"')
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_scripts_dir_path}/docwbmain/dbDataControlSetup.sql"')
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_scripts_dir_path}/docwbmain/dbDataTenantSetupDocwb.sql"')
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_scripts_dir_path}/workflow/dbDataSetup.sql"')
    __execute_command(commands)


def __create_user(config_data, sql_file_path):
    """Create user"""
    os.environ['PGPASSWORD'] = config_data["postgresPassword"]
    psql_command = "psql"
    psql_command += f' -p {config_data["postgresPortNum"]} -h {config_data["postgresHostIPAddress"]}'
    psql_command += f' -U {config_data["postgresUser"]}'
    postgres_dbname = config_data["postgresDBName"]

    ##### Step #####
    print(f"\n{Color.YELLOW}Inserting records{Color.DEFAULT}")
    commands = []
    commands.append(
        f'{psql_command} -d {postgres_dbname} -f "{sql_file_path}"')
    __execute_command(commands)


def do_processing(config_data):
    """Do processing"""
    input_data_dict = __parse_input()
    action = input_data_dict['action']
    print("")
    __validate_prerequisites()
    print("")
    if action == 'newdb':
        print(
            f"{Color.YELLOW}============= Create New Database ==========={Color.DEFAULT}")
        print(f"{Color.YELLOW}This script will create a new database using the following configuration.{Color.DEFAULT}")
        print("")
        __show_configuration(config_data)
        print("")
        print(f"{Color.YELLOW}NOTE: For any changes to above configuration, please update the following file.{Color.DEFAULT}")
        print(f"{Color.CYAN}{__file__}.{Color.DEFAULT}")
        print("")
        sql_scripts_dir_path = input_data_dict['sql_dir_path']
        user_input = input(
            f"{Color.YELLOW}Do you want to proceed? (yes/no): {Color.DEFAULT}")
        if user_input.lower() == 'yes':
            __create_database(config_data, sql_scripts_dir_path)
        else:
            print("Exiting without running the script.")
    elif action == 'newuser':
        print(
            f"{Color.YELLOW}============= Create New User ==========={Color.DEFAULT}")
        print(f"{Color.YELLOW}This script will create a new user using the following configuration.{Color.DEFAULT}")
        print("")
        __show_configuration(config_data)
        print("")
        print(f"{Color.YELLOW}NOTE: For any changes to above configuration, please update the following file.{Color.DEFAULT}")
        print(f"{Color.CYAN}{__file__}.{Color.DEFAULT}")
        print("")
        sql_file_path = input_data_dict['sql_file_path']
        user_input = input(
            f"{Color.YELLOW}Do you want to proceed? (yes/no): {Color.DEFAULT}")
        if user_input.lower() == 'yes':
            __create_user(config_data, sql_file_path)
        else:
            print("Exiting without running the script.")

    input("Press any key to exit...")


if __name__ == "__main__":
    # Uncomment for unit testing
    # SQL_DIR_PATH = r"C:\workarea\docwbsln\setup\db\sql"
    # sys.argv = ['<leave blank>',
    #             '--action',
    #             'newdb',
    #             '--sql_dir_path',
    #             SQL_DIR_PATH]

    # Uncomment for unit testing
    # SQL_FILE_PATH = r"C:\workarea\docwbsln\setup\db\sql\docwbmain\dbDataFirstUserSetup.sql"
    # sys.argv = ['<leave blank>',
    #             '--action',
    #             'newuser',
    #             '--sql_file_path',
    #             SQL_FILE_PATH]

    # "postgresHostIPAddress": "cvrimptmast9",
    CONFIG_DATA = {
        "postgresHostIPAddress": "localhost",
        "postgresPortNum": "5432",
        "postgresUser": "postgres",
        "postgresPassword": "pgdb123",
        "postgresDBName": "docwbdb",
        "postgresMaintenanceDBName": "postgres"
    }
    do_processing(CONFIG_DATA)
