import os
import glob
from data_preprocess import data_preprocess
from data_editor import content_replace, file_remove
from model_create import model_creation
from model_evaluate import model_evaluation

#Global Variable
parent_dir = os.path.dirname(os.path.dirname(__file__))
data_dir = os.path.join(parent_dir, 'dataset')
result_data_dir = os.path.join(data_dir, 'result_dataset')
saved_model_dir = os.path.join(parent_dir, 'saved_model')
TIME_STEP = 200

def prechecking():
    os.system('cls')
    if (os.path.exists(data_dir)):
        print('[INFO] Folder "dataset" exists in Folder "' + parent_dir +'"')
    else:
        print('[ERROR] Folder "dataset" is missing in Folder "' + parent_dir +'". Please download the "dataset" folder from the link in github.')
        return False
    if (os.path.exists(result_data_dir)):
        print('[INFO] Folder "result_dataset" exists in Folder "' + data_dir +'"')
    else:
        print('[ERROR] Folder "result_dataset" is missing in Folder "' + data_dir +'". Please create the corresponding folder with the correct name.')
        return False
    if (os.path.exists(saved_model_dir)):
        print('[INFO] Folder "saved_model" exists in Folder "' + parent_dir +'"')
    else:
        print('[ERROR] Folder "saved_model" is missing in Folder "' + parent_dir +'". Please create the corresponding folder with the correct name.')
        return False
    return True

def uiDisplay(title = '', option_list = [], extra_message = ''):
    os.system('cls')
    print(extra_message)
    size = len(option_list)
    print('Select a option for ' + title)
    for i in range(size):
        print(str(i+1) + '. ' + option_list[i])
    print(str(i+2) + '. Exit')
    option = input('Enter the corresponding number: ')
    return option

if prechecking():
    while True:
        option = uiDisplay('Start', ['Data Preprocessing', 'Data Editing', 'Model Training', 'Model Evaluation'])
        if option == '1':
            while True:
                all_folders = glob.glob(data_dir + '/**')
                user_count = 0
                data_count = 0
                for folder in all_folders:    
                    if folder != result_data_dir:
                        all_files = glob.glob(folder + '/*.csv')
                        user_count += 1 
                        data_count += len(all_files)
                option_2 = uiDisplay('Data Preprocessing', ['With Augmentation', 'No Augmentation'], 'Total of ' + str(user_count) + ' users with ' + str(data_count) + ' samples are detected.')
                if option_2 == '1':
                    file_name = input('Enter the name of the csv file: ') + '.csv'
                    file_dir = os.path.join(result_data_dir, file_name)
                    data_preprocess(data_dir, result_data_dir, file_dir, TIME_STEP, True)
                    input('Press any key to continue...')
                    break
                elif option_2 == '2':
                    file_name = input('Enter the name of the csv file: ') + '.csv'
                    file_dir = os.path.join(result_data_dir, file_name)
                    data_preprocess(data_dir, result_data_dir, file_dir, TIME_STEP, False)
                    input('Press any key to continue...')
                    break
                else:
                    break            
        elif option == '2':
            while True:
                option_2 = uiDisplay('Data Editing', ['Gesture Label Replace', 'Gesture Class Remove'])
                if option_2 == '1':
                    find = input('Enter the Label Name to replace:')
                    replace = input('Enter the new Label Name:')
                    confirm = input('Are you sure to replace label ' + find + ' to ' + replace + ' in all the sample files? (y/n):')
                    if confirm == 'y':
                        content_replace(data_dir, result_data_dir, find, replace)
                    input('Press any key to continue...')
                    break
                elif option_2 == '2':
                    target = input('Enter the Label Name to delete:')
                    confirm = input('Are you sure to remove all the ' + target + ' sample files? (y/n):')
                    if confirm == 'y':
                        file_remove(data_dir, result_data_dir, target)
                    input('Press any key to continue...')
                    break
                else:
                    break
        elif option == '3':
            while True:
                data = input('Enter the name of the preprocessed csv file in Folder "result_dataset": ')
                file_dir = os.path.join(result_data_dir, data + '.csv')
                if not(os.path.exists(file_dir)):
                    print ('[ERROR] File "' +  data + '.csv" is not found in Folder "' +  result_data_dir + '"')
                    input('Press any key to continue...')
                    break
                model_name = input('Enter the name of the model to be saved: ')
                option_2 = uiDisplay('Model Training', ['2D CNN', '1D CNN with Accel & Gyro', '1D CNN with Accel', '1D CNN with Gyro'])
                if option_2 == '1':
                    model_creation(file_dir, saved_model_dir, model_name, TIME_STEP, '2H')
                    input('Press any key to continue...')
                    break
                elif option_2 == '2':
                    model_creation(file_dir, saved_model_dir, model_name, TIME_STEP, '1H')
                    input('Press any key to continue...')
                    break               
                elif option_2 == '3':
                    model_creation(file_dir, saved_model_dir, model_name, TIME_STEP, '1H-A')
                    input('Press any key to continue...')
                    break
                elif option_2 == '4':
                    model_creation(file_dir, saved_model_dir, model_name, TIME_STEP, '1H-G')
                    input('Press any key to continue...')            
                    break
                else:
                    break
        elif option == '4':
            while True:
                model_name = input('Enter the name of the model in Folder "saved_model": ')
                model_dir = os.path.join(saved_model_dir, model_name + '.h5')
                if not(os.path.exists(model_dir)):
                    print ('[ERROR] File "' +  model_name + '.h5" is not found in Folder "' +  saved_model_dir + '"')
                    input('Press any key to continue...')
                    break
                data = input('Enter the name of the csv file in Folder "result_dataset": ')
                file_dir = os.path.join(result_data_dir, data + '.csv')
                if not(os.path.exists(file_dir)):
                    print ('[ERROR] File "' +  data + '.csv" is not found in Folder "' +  result_data_dir + '"')
                    input('Press any key to continue...')
                    break
                option_1 = uiDisplay('Data Split Required', ['Leave-one-person-out cross-validation (Split the last users for testing)', 'Use all data for testing (Make sure the data was not used for training)'])
                if option_1 == '2': split = False 
                else: split = True
                option_2 = uiDisplay('Model Evaluation', ['2D CNN', '1D CNN with Accel & Gyro', '1D CNN with Accel', '1D CNN with Gyro'])
                if option_2 == '1':
                    model_evaluation(model_dir, file_dir, TIME_STEP, '2H', split)
                    input('Press any key to continue...')
                    break
                elif option_2 == '2':
                    model_evaluation(model_dir, file_dir, TIME_STEP, '1H', split)
                    input('Press any key to continue...')
                    break
                elif option_2 == '3':
                    model_evaluation(model_dir, file_dir, TIME_STEP, '1H-A', split)
                    input('Press any key to continue...')
                    break
                elif option_2 == '4':
                    model_evaluation(model_dir, file_dir, TIME_STEP, '1H-G', split)
                    input('Press any key to continue...')
                    break
                else:
                    break
        else:
            break