import os
import glob

def content_replace(path, result_path, find, replace):
    all_folders = glob.glob(path + '/**')
    for folder in all_folders:
        if folder != result_path:
            all_files = glob.glob(folder + '/*.csv')
            for file in all_files:
                if find in file:
                    print('[INFO] ' + file + ': Replaced (' + find + ') to (' + replace + ').')
                    with open(file, 'r') as f:
                        newText = f.read().replace(find, replace)
                    newFileName = file.replace(find, replace)
                    open(newFileName, 'w').write(newText)
                    os.remove(file)

def file_remove(path, result_path, find):
    all_folders = glob.glob(path + '/**')
    for folder in all_folders:
        if folder != result_path:
            all_files = glob.glob(folder + '/*.csv')
            for file in all_files:
                if find in file:
                    print('[INFO] ' + file + ': Deleted.')
                    os.remove(file)