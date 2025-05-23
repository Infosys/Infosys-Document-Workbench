# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import platform
import shutil

# List of directories to create
directories = [
    "/nfs/docwbdx/data/input/t01",
    "/nfs/docwbdx/data/input/t02",
    "/nfs/docwbdx/data/work",
    "/nfs/docwbdx/data/config",
    "/nfs/docwbdx/data/output",
    "/nfs/docwbdx/data/cache",
    "/nfs/docwbdx/data/temp",
    "/nfs/docwbdx/logs",
    "/nfs/docwbdx/docwbdxfiledb",
    "/nfs/docwbdx/docwbvectordb",
    "/home/projadmin/workarea/docwbsln/services",
    "/home/projadmin/workarea/docwbsln/logs",
    "/home/projadmin/workarea/docwbsln/setup/db/ddl/main",
    "/home/projadmin/workarea/docwbsln/setup/certificates",
    "/home/projadmin/workarea/docwbsln/data/attachmentsdb",
    "/home/projadmin/workarea/docwbsln/data/temp",
    "/home/projadmin/workarea/docwbsln/apps",
    "/home/projadmin/workarea/docwbsln/tomcatapps",
    "/home/projadmin/myprogramfiles/AI/models/tessdata",
    "/home/projadmin/myprogramfiles/AI/models/tiktoken_encoding",
    "/home/projadmin/myprogramfiles/InfyFormatConverter",
    "/home/projadmin/myprogramfiles/InfyOcrEngine",
]

# Function to create directories and set permissions on Linux
def create_directories_linux():
    # Ensure /nfs directory exists
    if not os.path.exists("/nfs"):
        os.makedirs("/nfs", exist_ok=True)
    if not os.path.exists("/home"):
        os.makedirs("/home", exist_ok=True)
    # Create each directory
    for directory in directories:
        os.makedirs(directory, exist_ok=True)
        print(f"Created directory: {directory}")

    # Set full permissions and change ownership for /nfs and all its subdirectories and files
    os.system("sudo chmod -R 777 /nfs")
    os.system("sudo chown -R projadmin:projadmin /nfs")
    os.system("sudo chown -R projadmin:projadmin /home/projadmin/workarea/docwbsln")
    os.system("sudo chown -R projadmin:projadmin /home/projadmin/myprogramfiles")

    print("All directories and files have been created successfully with permissions and ownership.")

# Function to create directories on Windows
def create_directories_windows():
    # Remove /home/projadmin prefix from directories
    windows_directories = [d.replace("/home/projadmin", "") for d in directories]
    
    # Create each directory
    for directory in windows_directories:
        os.makedirs(directory, exist_ok=True)
        print(f"Created directory: {directory}")

    # Inform user about the copy operation
    print("FYI - Please note that you will be copying a set of files and folders from C:\\InfyDocWB\\components\\python\\apps\\config to the C:/nfs/docwbdx/data/config directory.")
    
    # Ask for user confirmation
    user_input = input("Do you want to proceed? (yes/no): ")
    
    if user_input.lower() == 'yes':
        # Copy specified folders and files to /nfs/docwbdx/data/config
        # Please adjust the source and destination paths as needed
        source_config_path = r"C:/InfyDocWB/components/python/apps/config"
        destination_config_path = r"/nfs/docwbdx/data/config"
        
        folders_to_copy = ["batch_extractor_script", "prompt_templates", "templates"]
        
        for folder in folders_to_copy:
            src_folder = os.path.join(source_config_path, folder)
            dest_folder = os.path.join(destination_config_path, folder)
            if os.path.exists(src_folder):
                shutil.copytree(src_folder, dest_folder, dirs_exist_ok=True)
                print(f"Copied folder: {src_folder} to {dest_folder}")
        
        # Copy all .json files
        for file_name in os.listdir(source_config_path):
            if file_name.endswith(".json"):
                src_file = os.path.join(source_config_path, file_name)
                dest_file = os.path.join(destination_config_path, file_name)
                shutil.copy2(src_file, dest_file)
                print(f"Copied file: {src_file} to {dest_file}")

        print("All specified folders and files have been copied successfully.")
    else:
        print("Copy operation aborted by the user.")

# Main function to start the script
def main():
    user_input = input("Do you want to start the directory creation ? (yes/no): ")
    if user_input.lower() == 'yes':
        # Check the operating system and call the appropriate function
        if platform.system() == "Linux":
            create_directories_linux()
        elif platform.system() == "Windows":
            create_directories_windows()
        else:
            print("Unsupported operating system.")
    else:
        print("Script execution aborted by the user.")

if __name__ == "__main__":
    main()