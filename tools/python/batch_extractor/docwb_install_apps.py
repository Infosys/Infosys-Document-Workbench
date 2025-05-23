# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import subprocess
import os
import platform

# List of app directories
if platform.system() == 'Windows':
    app_dirs = [
        r'C:\workarea\docwbsln\apps\infy_dx_downloader',
        r'C:\workarea\docwbsln\apps\infy_dx_preprocessor',
        r'C:\workarea\docwbsln\apps\infy_dx_indexer',
        r'C:\workarea\docwbsln\apps\infy_dx_extractor',
        r'C:\workarea\docwbsln\apps\infy_dx_postprocessor',
        r'C:\workarea\docwbsln\apps\infy_dx_casecreator',
    ]
else:
    app_dirs = [
        '/home/projadmin/workarea/docwbsln/apps/infy_dx_downloader',
        '/home/projadmin/workarea/docwbsln/apps/infy_dx_preprocessor',
        '/home/projadmin/workarea/docwbsln/apps/infy_dx_indexer',
        '/home/projadmin/workarea/docwbsln/apps/infy_dx_extractor',
        '/home/projadmin/workarea/docwbsln/apps/infy_dx_postprocessor',
        '/home/projadmin/workarea/docwbsln/apps/infy_dx_casecreator',
    ]

# Artifactory settings
artifactory_settings = "--index-url <Artifactory_URL> --trusted-host <Artifactory_Host>"

# Function to execute a shell command
def run_command(command, cwd=None):
    result = subprocess.run(command, shell=True, cwd=cwd,
                            text=True, capture_output=True)
    if result.returncode != 0:
        print(f"Error executing command: {command}\n{result.stderr}")
    else:
        print(f"Successfully executed command: {command}\n{result.stdout}")

# Main function to set up virtual environments and install packages
def setup_virtualenvs(app_dirs):
    for app_dir in app_dirs:
        print(f"Setting up virtual environment for {app_dir}")
        
        # Change to app directory
        os.chdir(app_dir)
        
        # Create virtual environment
        run_command('python -m venv .venv')
        
        # Activate virtual environment
        if platform.system() == 'Windows':
            activate_script = '.\\.venv\\Scripts\\activate.bat && '
            deactivate_script = '.\\.venv\\Scripts\\deactivate.bat'
        else:
            activate_script = 'source ./.venv/bin/activate && '
            deactivate_script = 'deactivate'

        # Upgrade pip
        run_command(activate_script +
                    'python -m pip install --upgrade pip')

        # Install requirements and deactivate virtual environment
        run_command(
            activate_script + 'python -m pip install -r requirements.txt --no-cache-dir && ' + deactivate_script)


if __name__ == '__main__':
    setup_virtualenvs(app_dirs)