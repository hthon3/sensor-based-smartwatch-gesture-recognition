import seaborn as sns
import tensorflow as tf
import numpy as np
import pandas as pd
from scipy import stats
from matplotlib import pyplot as plt
from sklearn import metrics
from sklearn import preprocessing
from sklearn.metrics import classification_report
from keras.utils import np_utils

LABEL = "ActivityEncoded"
le = preprocessing.LabelEncoder()

def read_data(file_path):
    column_names = ['accel_x','accel_y','accel_z','gyro_x','gyro_y','gyro_z','activity','userid']
    df = pd.read_csv(file_path,header = None, names = column_names)
    return df

def create_segments_and_labels(df, time_steps, label_name):
    all_segments = []
    accel_segments = []
    gyro_segments = []
    labels = []
    for i in range(0, len(df) - time_steps + 1, time_steps):
        a_xs = df['accel_x'].values[i: i + time_steps]
        a_ys = df['accel_y'].values[i: i + time_steps]
        a_zs = df['accel_z'].values[i: i + time_steps]
        g_xs = df['gyro_x'].values[i: i + time_steps]
        g_ys = df['gyro_y'].values[i: i + time_steps]
        g_zs = df['gyro_z'].values[i: i + time_steps]
        label = stats.mode(df[label_name][i: i+ time_steps])[0][0]
        all_segments.append([a_xs, a_ys, a_zs, g_xs, g_ys, g_zs])
        accel_segments.append([a_xs, a_ys, a_zs])
        gyro_segments.append([g_xs, g_ys, g_zs])
        labels.append(label)
    reshaped_all_segments = np.asarray(all_segments, dtype= np.float32).reshape(-1, time_steps, 6)
    reshaped_accel_segments = np.asarray(accel_segments, dtype= np.float32).reshape(-1, time_steps, 3)
    reshaped_gyro_segments = np.asarray(gyro_segments, dtype= np.float32).reshape(-1, time_steps, 3)
    labels = np.asarray(labels)
    return reshaped_all_segments, reshaped_accel_segments, reshaped_gyro_segments, labels

def train_test_split(df):
    split = 1
    df_test = df[df['userid'] < split]
    df_train = df[df['userid'] >= split]
    return df_train, df_test 

def data_initial(data_path, split = True):
    df = read_data(data_path)    
    df[LABEL] = le.fit_transform(df['activity'].values.ravel())
    if split == True:
        df_train, df_test = train_test_split(df)
        LABELS = df_train['activity'].unique()
        return df_train, df_test, LABELS
    else:
        LABELS = df['activity'].unique()
        return df, df, LABELS

def show_confusion_matrix(validations, predictions, size, LABELS):
    matrix = metrics.confusion_matrix(validations, predictions)
    plt.figure()
    sns.heatmap(matrix/size,
                cmap="coolwarm",
                linecolor='white',
                linewidths=1,
                xticklabels=LABELS,
                yticklabels=LABELS,
                annot=True,
                fmt=".0%")
    plt.title("Gesture Confusion Matrix")
    plt.ylabel("True Label")
    plt.xticks(rotation=45)
    plt.xlabel("Predicted Label")
    plt.subplots_adjust(left=0.125, bottom=0.2, right=1, top=0.95, wspace=0.2, hspace=0.2)
    plt.show()

def model_evaluation(model_path, data_path, time_step, type = '2H', split = True):
    model = tf.keras.models.load_model(model_path)
    df_train, df_test, LABELS = data_initial(data_path, split)
    x_test, x_test_a, x_test_g, y_test = create_segments_and_labels(df_test, time_step, LABEL)
    num_classes = le.classes_.size
    y_test = y_test.astype('float32')
    y_test = np_utils.to_categorical(y_test, num_classes)
    if type == '1H':
        num_time_periods, num_sensors = x_test.shape[1], x_test.shape[2]
        input_shape = num_time_periods*num_sensors
        x_test = x_test.reshape(x_test.shape[0], input_shape)
        x_test = x_test.astype('float32')
        print('Label Shape: ' + str(y_test.shape))
        print('Testing Data Shape: ' + str(x_test.shape))
    else:
        num_time_periods, num_sensors = x_test_a.shape[1], x_test_a.shape[2]
        input_shape = num_time_periods*num_sensors
        x_test_a = x_test_a.reshape(x_test_a.shape[0], input_shape)
        x_test_a = x_test_a.astype('float32')
        x_test_g = x_test_g.reshape(x_test_g.shape[0], input_shape)
        x_test_g = x_test_g.astype('float32')
        print('Label Shape: ' + str(y_test.shape))
        if type == '1H-A' or type == '1H-G':
            print('Testing Data Shape: ' + str(x_test_a.shape))
        else:
            print('Testing Data Shape: ' + str(x_test_a.shape) + " ," + str(x_test_a.shape))

    if type == "1H":
        score = model.evaluate(x_test, y_test, verbose=1)
        y_pred_test = model.predict(x_test)
    elif type == "1H-A":
        score = model.evaluate(x_test_a, y_test, verbose=1)
        y_pred_test = model.predict(x_test_a)        
    elif type == "1H-G":
        score = model.evaluate(x_test_g, y_test, verbose=1)
        y_pred_test = model.predict(x_test_g) 
    else:
        score = model.evaluate([x_test_a, x_test_g], y_test, verbose=1)
        y_pred_test = model.predict([x_test_a, x_test_g]) 

    print('\nAccuracy on test data: %0.4f' % score[1])
    print('\nLoss on test data: %0.4f' % score[0])
    max_y_pred_test = np.argmax(y_pred_test, axis=1)
    max_y_test = np.argmax(y_test, axis=1)
    show_confusion_matrix(max_y_test, max_y_pred_test, df_test.shape[0]/4200, LABELS)
    print('\n--- Classification report for test data ---\n')
    print(classification_report(max_y_test, max_y_pred_test, digits=4))