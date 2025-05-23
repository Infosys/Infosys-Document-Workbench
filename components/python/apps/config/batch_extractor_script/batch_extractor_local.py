# ===============================================================================================================#
# Copyright 2024 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import subprocess
import os
import random
import string


print("Please choose the environment:")
print("1. local")
choice = input("Enter your choice (1): ")
if choice == '1':
    ENVIRONMENT = "local"
else:
    print("Invalid choice. Setting default environment: local.")
    ENVIRONMENT = "local"

# Change only base_path if the project is located in a different directory
if ENVIRONMENT == "local":
    base_path = r"C:/workarea/docwbsln/apps"
    output_path = r"C:/nfs/docwbdx/data/output"
    root_path = r"C:/nfs/docwbdx/data"

script_name = r"app_executor_container.py"

uuid_format = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'
hex_digits = string.hexdigits.lower()
uuid = ''.join([random.choice(hex_digits) if x ==
               'x' else x for x in uuid_format])
uuid = uuid[:14] + '4' + uuid[15:]
uuid = uuid[:19] + random.choice('89ab') + uuid[20:]
request_id = 'R-'+uuid

master_config_path = r"config/master_client_config.json"

# venv - Update {UserId} with your system userId.(Ex:rkumar.t21)
venv_path_infy_dx_downloader = r"C:/Users/{UserId}/.virtualenvs/infy_dx_downloader-mLqCgFZ4/Scripts/python.exe"
venv_path_infy_dx_preprocessor = r"C:/Users/{UserId}/.virtualenvs/infy_dx_preprocessor-6d-EF32l/Scripts/python.exe"
venv_path_infy_dx_indexer = r"C:/Users/{UserId}/.virtualenvs/infy_dx_indexer-aW7xfPHI/Scripts/python.exe"
venv_path_infy_dx_extractor = r"C:/Users/{UserId}/.virtualenvs/infy_dx_extractor-VYzbY46k/Scripts/python.exe"
venv_path_infy_dx_postprocessor = r"C:/Users/{UserId}/.virtualenvs/infy_dx_postprocessor-57vnidI3/Scripts/python.exe"
venv_path_infy_dx_casecreator = r"C:/Users/{UserId}/.virtualenvs/infy_dx_casecreator-10aeq_gH/Scripts/python.exe"

executions = [
    {
        "enable": True,
        "project_name": r"infy_dx_downloader",
        "venv_path": venv_path_infy_dx_downloader,
        "args": []
    },
    {
        "enable": True,
        "project_name": r"infy_dx_preprocessor",
        "venv_path": venv_path_infy_dx_preprocessor,
        "args": ["--request_file", r"_downloader_response.json"]
    },
    {
        "enable": True,
        "project_name": r"infy_dx_indexer",
        "venv_path": venv_path_infy_dx_indexer,
        "args": ["--preprocessor_outfile_path", r"_pre_processor_response.json"]
    },
    {
        "enable": True,
        "project_name": r"infy_dx_extractor",
        "venv_path": venv_path_infy_dx_extractor,
        "args": ["--indexer_outfile_path", r"_indexer_response.json"]
    },
    {
        "enable": True,
        "project_name": r"infy_dx_postprocessor",
        "venv_path": venv_path_infy_dx_postprocessor,
        "args": ["--extractor_outfile_path", r"_extractor_response.json"]
    },
    {
        "enable": True,
        "project_name": r"infy_dx_casecreator",
        "venv_path": venv_path_infy_dx_casecreator,
        "args": ["--postprocessor_outfile_path", r"_post_processor_response.json"]
    }
]

execution_results = []
master_config_path = root_path + '/' + master_config_path
for execution in executions:
    if execution["enable"]:
        print("------- Starting execution for:",
              execution["project_name"], "-------")
        full_project_path = base_path + '/' + \
            execution["project_name"] + '/src'
        os.chdir(full_project_path)
        print("Navigating to Dir:")
        print(full_project_path)

        env_file_path = base_path + '/' + execution["project_name"] + '/.env'
        env_vars = os.environ.copy()
        if os.path.exists(env_file_path):
            with open(env_file_path) as f:
                for line in f:
                    if line.startswith('#') or not line.strip():
                        continue
                    key, value = line.strip().split('=', 1)
                    env_vars[key] = value
            print(".env file loaded for:")
            print(env_file_path)

        command = f'{execution["venv_path"]} {script_name} --request_id {request_id} --master_config {master_config_path}'
        if "args" in execution and execution["args"]:
            for arg in execution["args"]:
                if arg.startswith('_'):
                    arg = request_id + arg
                    arg = output_path + '/' + arg
                command += " " + arg
        print("Running command:")
        print(command)

        process = subprocess.Popen(
            command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=env_vars)
        output, error = process.communicate()
        print("Output:", output)
        print("Script Error:", error)

        status = ' - Success' if process.returncode == 0 else ' - Failed'
        project_name = execution["project_name"].replace(base_path + '/', '')
        execution_results.append(f"{project_name}{status}")

        if process.returncode != 0:
            print(f"Error occurred while executing command: {execution}")
            break
        print("-------Execution completed for:",
              execution["project_name"], "-------")

print("Status:")
for result in execution_results:
    print(result)
print("Execution completed.")
print("NOTE: In case of any failed processes, please check the logs from following path: ",
      output_path.replace('/data/output', '/logs'))
