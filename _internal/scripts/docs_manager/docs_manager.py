# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


import os
import subprocess
import time
import glob

VERSION = '0.0.1'


class Color():
    DEFAULT = '\033[0m'
    YELLOW = '\033[93m'
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    GRAY = '\033[90m'


class ShellUtil():
    def run_commands_batch(self, cmds) -> None:
        print(f"{Color.CYAN}WORKING DIR: {os.getcwd()}{Color.DEFAULT}")
        cmd = " && ".join(cmds)
        self.__execute_command(cmd)

    def run_commands_serial(self, cmds) -> None:
        print(f"{Color.CYAN}WORKING DIR: {os.getcwd()}{Color.DEFAULT}")
        for cmd in cmds:
            print(f"{Color.GREEN}{cmd}{Color.DEFAULT}")
            self.__execute_command(cmd)

    def __execute_command(self, cmd):
        process = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE, text=True)
        while True:
            output = process.stdout.readline()
            if output == '' and process.poll() is not None:
                break
            if output:
                print(f"{Color.GRAY}{output.strip()}{Color.DEFAULT}")
        rc = process.poll()
        if rc != 0:
            error_output = process.stderr.read()
            if error_output:
                print(f"{Color.RED}{error_output.strip()}{Color.DEFAULT}")


class FileWatcher:
    def __init__(self, search_pattern: str, interval: int = 1):
        self.__interval = interval
        self.__search_pattern = search_pattern
        self.__files_snapshot = self.__take_snapshot()

    def __take_snapshot(self):
        files = {f: os.path.getmtime(f) for f in glob.glob(self.__search_pattern,
                                                           recursive=True)}
        return files

    def __get_dtm(self):
        return time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())

    def start_watch(self, payload_fn):
        print(
            f"{Color.CYAN}{self.__get_dtm()} Watching directory: {self.__search_pattern}{Color.DEFAULT}")
        while True:
            time.sleep(self.__interval)
            new_snapshot = self.__take_snapshot()
            added = [f for f in new_snapshot if f not in self.__files_snapshot]
            removed = [
                f for f in self.__files_snapshot if f not in new_snapshot]
            modified = [
                f for f in new_snapshot if f in self.__files_snapshot
                and new_snapshot[f] != self.__files_snapshot[f]]

            if added:
                print(
                    f"{Color.GRAY}{self.__get_dtm()} Added: {', '.join(added)}{Color.DEFAULT}")
            if removed:
                print(
                    f"{Color.GRAY}{self.__get_dtm()} Removed: {', '.join(removed)}{Color.DEFAULT}")
            if modified:
                print(
                    f"{Color.GRAY}{self.__get_dtm()} Modified: {', '.join(modified)}{Color.DEFAULT}")
            self.__files_snapshot = new_snapshot

            if any([added, removed, modified]):
                print(f"{Color.GRAY}{self.__get_dtm()} Task started{Color.DEFAULT}")
                payload_fn()
                print(
                    f"{Color.GRAY}{self.__get_dtm()} Task completed{Color.DEFAULT}")


class DocsLauncher():
    def create_virtual_environment(self):
        cmd_str = r"""
        python -m venv .venv
        .venv\Scripts\python -m pip install --upgrade pip
        """
        self.__run_command(cmd_str)

    def install_packages(self):
        cmd_str = r"""
        .venv\Scripts\python -m pip install .
        """
        self.__run_command(cmd_str)

    def build_docs(self):
        cmd_str = r"""
        .venv\Scripts\activate.bat
        docs\make.bat clean
        docs\make.bat html
        """
        self.__run_command(cmd_str, is_batch=True)

    def launch_docs(self):
        mode = 'local'
        if mode == 'localhost':
            port = self.__get_free_port()
            home_page_url = f"http://localhost:{port}"
            print(
                f"{Color.GRAY}Documentation launched from {home_page_url}{Color.DEFAULT}")
            cmd_str = rf"""
            .venv\Scripts\python -m http.server --dir docs\build\html -p {port}
            """
            self.__run_command(cmd_str)
        else:
            home_page_path = r"docs\build\html\index.html"
            cmd_str = fr"""
            start {home_page_path}
            """
            self.__run_command(cmd_str)
            print(
                f"{Color.GRAY}Documentation launched from {home_page_path}{Color.DEFAULT}")

    def __run_command(self, cmd_str, is_batch=False):
        cmds = [y for y in [x.strip()
                            for x in cmd_str.split('\n')] if len(y) > 0]
        if is_batch:
            ShellUtil().run_commands_batch(cmds)
        else:
            ShellUtil().run_commands_serial(cmds)

    def __get_free_port(self):
        import socket
        for port in range(8000, 8100):
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.bind(('', port))
                sock.close()
                time.sleep(5)
                print(f"{Color.GRAY}Port {port} is available{Color.DEFAULT}")
                return port
            except:
                print(f"{Color.GRAY}Port {port} is in use{Color.DEFAULT}")
        return None


def fw_payload():
    DocsLauncher().build_docs()


def show_recursive_menu():

    print(f'{Color.YELLOW}\n{"*"*30}\nDocs Manager\n{"*"*30}{Color.DEFAULT}')
    print(f'{Color.CYAN}This script takes care of generation of a documentation site (using Sphinx).{Color.DEFAULT}')
    print(f'{Color.CYAN}The generated site can be used to follow further instructions.{Color.DEFAULT}')
    print('')

    search_pattern = "./docs/source/**/*"
    file_watcher = FileWatcher(search_pattern, interval=5)

    user_input = None
    while not user_input == 'q':
        print('')
        print(
            f'{Color.YELLOW}================ Select Option to Continue ========={Color.DEFAULT}')
        print('')
        print(
            f'{Color.YELLOW}>>>>>>>>>> INSTALLATION (One-time only) >>>>>>>>>> {Color.DEFAULT}')
        print(f'{Color.YELLOW}1. Create virtual environment{Color.DEFAULT}')
        print(f'{Color.YELLOW}2. Install packages{Color.DEFAULT}')
        print(f'{Color.YELLOW}3. Build documentation{Color.DEFAULT}')
        print('')
        print(
            f'{Color.YELLOW}5. Quick start (1 + 2 + 3){Color.DEFAULT}')
        print('')
        print(
            f'{Color.YELLOW}>>>>>>>>>> EXECUTION >>>>>>>>>> {Color.DEFAULT}')
        print(f'{Color.YELLOW}11. View documentation{Color.DEFAULT}')
        print(f'{Color.YELLOW}12. Rebuild documentation{Color.DEFAULT}')
        print(f'{Color.YELLOW}13. Rebuild documentation (Auto){Color.DEFAULT}')
        print('')

        print(f'{Color.YELLOW}Q. Exit{Color.DEFAULT}')
        print('')
        print('')
        user_input = input('Please make a selection: ')
        if str.isdigit(user_input):
            user_input = int(user_input)
        else:
            user_input = user_input.lower()

        if user_input == 1:
            DocsLauncher().create_virtual_environment()
        elif user_input == 2:
            DocsLauncher().install_packages()
        elif user_input in [3, 12]:
            DocsLauncher().build_docs()
        elif user_input == 5:
            DocsLauncher().create_virtual_environment()
            DocsLauncher().install_packages()
            DocsLauncher().build_docs()
        elif user_input == 11:
            DocsLauncher().launch_docs()
        elif user_input == 13:
            file_watcher.start_watch(fw_payload)


if __name__ == '__main__':
    # Uncomment for unit testing
    # os.chdir(r'C:\<My-Root-Dir-Containing-pyproject.toml-File>')
    show_recursive_menu()
