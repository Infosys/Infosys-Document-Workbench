# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


import argparse
import tkinter as tk
from tkinter import ttk, messagebox
import json
import local_cache_util as lcu

INPUT_DATA_DICT = {
    "config_file_path": None,
}


class Label(tk.Label):
    def __init__(self, master=None, word_wrap=False, **kwargs):
        tk.Label.__init__(self, master, **kwargs)
        if word_wrap:
            self.bind('<Configure>', lambda e: self.config(
                wraplength=self.winfo_width()))


class ConfigApp:
    __TITLE = "Local Cache Manager"

    def __init__(self, root, config_data):

        root.title(f"{self.__TITLE} :: {config_data['name']}")
        root.geometry("1200x600")

        root.grid_columnconfigure(0, weight=1)  # Allow column 0 to expand
        root.grid_rowconfigure(0, weight=1)     # Allow row 0 to expand

        self.config_data = config_data

        self.frm_grid = tk.Frame(
            root, bd=0, relief="solid")
        # self.frm_grid.configure(bg='yellow')
        self.frm_grid.pack(fill=tk.BOTH, expand=True)

        # Add scrollbars
        self.canvas = tk.Canvas(self.frm_grid)
        # self.canvas.configure(bg='blue')
        self.canvas.grid_configure(row=0, column=0, sticky="nsew")
        self.canvas.pack(fill=tk.BOTH, expand=True, pady=10)

        self.scrollbar_y = ttk.Scrollbar(
            self.frm_grid, orient="vertical", command=self.canvas.yview)
        self.scrollbar_y.pack(side=tk.RIGHT, fill=tk.Y)
        # self.scrollbar_x = ttk.Scrollbar(
        #     self.frm_grid, orient="horizontal", command=self.canvas.xview)

        self.scrollable_frame = ttk.Frame(self.canvas)
        # style = ttk.Style()
        # style.configure('Custom.TFrame', background='pink')
        # self.scrollable_frame.configure(style='Custom.TFrame')

        self.scrollable_frame.pack(fill=tk.BOTH, expand=True)

        self.scrollable_frame.bind(
            "<Configure>",
            lambda e: self.canvas.configure(
                scrollregion=self.canvas.bbox("all")
            )
        )

        self._frame_id = self.canvas.create_window(
            (0, 0), window=self.scrollable_frame, anchor="nw")
        self.canvas.configure(
            yscrollcommand=self.scrollbar_y.set
            # , xscrollcommand=self.scrollbar_x.set
        )

        self.canvas.pack(side="left", fill=tk.BOTH, expand=True)

        self.canvas.bind("<Configure>", self.on_canvas_resize)

        self.scrollbar_y.pack(side="right", fill="y")
        # self.scrollbar_x.pack(side="bottom", fill="x")

        # Create the grid
        self.__create_grid()

        self.__load_properties()

        self.root = root

    def on_canvas_resize(self, event):
        # Update frame width to fill the canvas
        self.canvas.itemconfig(
            self._frame_id, width=event.width)

    def on_submit(self):
        self.__save_properties()
        self.__check_properties()
        messagebox.showinfo("Confirmation", "Data saved successfully!")

    def __save_properties(self):
        bucket = self.config_data['user_cache_manager']['bucket']
        for item in self.__text_value_list:
            for key, obj in item.items():
                _value = obj.get("1.0", tk.END).strip()
                # print(f"{key}: {_value}")
                local_cache_util.set(bucket, key, _value)

    def __load_properties(self):
        bucket = self.config_data['user_cache_manager']['bucket']
        for item in self.__text_value_list:
            for key, obj_text in item.items():
                value_cache, error = local_cache_util.get(bucket, key)
                obj_label = [
                    x for x in self.__label_key_list if key in x][0]
                key_str = key
                if not error:
                    obj_text.delete(1.0, tk.END)
                    obj_text.insert(tk.END, value_cache)
                    key_str = key + " ✓"
                obj_label[key].configure(text=key_str)

    def __check_properties(self) -> int:
        bucket = self.config_data['user_cache_manager']['bucket']
        count_mismatch = 0
        for item in self.__text_value_list:
            for key, obj in item.items():
                _value = obj.get("1.0", tk.END).strip()
                # print(f"{key}: {_value}")
                value_cache, error = local_cache_util.get(bucket, key)
                obj_label = [
                    x for x in self.__label_key_list if key in x][0]
                key_str = key
                if _value == value_cache:
                    key_str = key + " ✓"
                else:
                    count_mismatch += 1
                obj_label[key].configure(text=key_str)

        return count_mismatch

    def on_cancel(self):
        unsaved_items = self.__check_properties()
        if unsaved_items > 0:
            message = f"{unsaved_items} unsaved item(s) found. \nDo you want to exit without saving?"
            answer = messagebox.askquestion(
                "Confirimation", message, default='no')
            if answer != 'yes':
                return
        self.root.destroy()

    def __create_grid(self):
        # Configure column weights
        self.scrollable_frame.grid_columnconfigure(0, weight=0)
        self.scrollable_frame.grid_columnconfigure(1, weight=0)
        self.scrollable_frame.grid_columnconfigure(2, weight=0)
        self.scrollable_frame.grid_columnconfigure(3, weight=1)

        properties = self.config_data['properties']
        row_count = len(properties)
        for i in range(row_count):
            self.scrollable_frame.grid_rowconfigure(i, weight=1)

        self.__text_value_list = []
        self.__label_key_list = []
        for i, item in enumerate(properties):
            value_default = item['value_default']
            key_str = item['key']
            # value_cache, error = local_cache_util.get(
            #     self.config_data['user_cache_manager']['bucket'], item['key'])

            # value = value_cache if not error else value_default
            # key_str = item['key'] + "*" if error else item['key']
            label_sno = Label(self.scrollable_frame,
                              text=f"{str(i+1)}.", anchor="e")
            # label_sno.configure(bg='lightgreen')
            label_sno.grid(row=i, column=0, padx=10, pady=5, sticky="nsew")

            label_key = Label(self.scrollable_frame, word_wrap=True,
                              text=key_str, anchor="w")
            # label.configure(bg='lightgreen')
            label_key.grid(row=i, column=1, padx=10, pady=5, sticky="nsew")

            text_value = tk.Text(self.scrollable_frame, width=70,
                                 height=1)
            # entry.configure(bg='lightgreen')
            text_value.grid(row=i, column=2, padx=10, pady=5, sticky="nsew")
            text_value.insert(tk.END, value_default)

            label_comment = Label(self.scrollable_frame, word_wrap=True,
                                  text=item['comment'], anchor="w", justify="left")

            # comment.configure(bg='lightgreen')
            label_comment.grid(row=i, column=3, padx=10, pady=5, sticky="nsew")

            self.__text_value_list.append({
                item['key']: text_value
            })
            self.__label_key_list.append({
                item['key']: label_key
            })

        self.frm_buttons = tk.Frame(
            self.scrollable_frame, bd=0, relief="solid")
        self.frm_buttons.grid(row=row_count+1, column=0, columnspan=4,
                              sticky="nsew", pady=20)
        self.frm_buttons.grid_columnconfigure(0, weight=1)
        self.frm_buttons.grid_columnconfigure(1, weight=1)
        self.frm_buttons.grid_rowconfigure(0, weight=1)
        self.frm_buttons.grid_rowconfigure(1, weight=1)

        self.btn_submit = tk.Button(
            self.frm_buttons, text="SAVE", command=self.on_submit)
        self.btn_submit.grid(row=1, column=0, padx=10,
                             pady=(0, 10), sticky="e")
        self.btn_cancel = tk.Button(
            self.frm_buttons, text="CANCEL", command=self.on_cancel)
        self.btn_cancel.grid(row=1, column=1,
                             padx=10, pady=(0, 10), sticky="w")


local_cache_util = lcu.LocalCacheUtil()


def __parse_input():
    input_data_dict = INPUT_DATA_DICT.copy()
    parser = argparse.ArgumentParser()
    parser.add_argument("--config_file_path", "-c",
                        default=None, required=True)
    args = parser.parse_args()
    input_data_dict['config_file_path'] = args.config_file_path
    return input_data_dict


def do_processing():
    input_data_dict = __parse_input()
    config_file_path = input_data_dict['config_file_path']
    with open(config_file_path, 'r') as file:
        data = json.load(file)
    root = tk.Tk()
    root.state('zoomed')
    app = ConfigApp(root, data)
    root.mainloop()


if __name__ == "__main__":
    # Uncomment for unit testing
    # import sys
    # cmd_str = "<leave_blank> --config_file_path local_cache_manager/lcm_sample_config.json"
    # sys.argv = cmd_str.split()
    do_processing()
