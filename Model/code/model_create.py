import os
import numpy as np
import pandas as pd
import tensorflow as tf
from cnn_1h import cnn_model_1h
from cnn_2h import cnn_model_2h
from model_evaluate import model_evaluation
from scipy import stats
from keras.callbacks import ModelCheckpoint
from keras.utils import np_utils
from sklearn import preprocessing

LABEL = 'ActivityEncoded'
le = preprocessing.LabelEncoder()

def read_data(file_path):
    column_names = ['accel_x','accel_y','accel_z','gyro_x','gyro_y','gyro_z','activity','userid']
    df = pd.read_csv(file_path,header = None, names = column_names)
    return df

def show_basic_dataframe_info(dataframe):
    print('[INFO] Number of columns in the dataframe: %i' % (dataframe.shape[1]))
    print('[INFO] Number of rows in the dataframe: %i\n' % (dataframe.shape[0]))

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

def model_creation(data_path, result_path, model_name, time_step, type = '2H'):
    #Parameter
    tr_model_path = os.path.join(result_path, model_name + '.h5')
    trl_model_path = os.path.join(result_path, model_name + '.tflite')
    #Data Initial Stage
    df_train, df_test, LABELS = data_initial(data_path)
    x_train, x_train_a, x_train_g, y_train = create_segments_and_labels(df_train, time_step, LABEL)
    num_classes = le.classes_.size
    y_train = y_train.astype('float32')
    y_train = np_utils.to_categorical(y_train, num_classes)
    if type == '1H':
        num_time_periods, num_sensors = x_train.shape[1], x_train.shape[2]
        input_shape = num_time_periods*num_sensors
        x_train = x_train.reshape(x_train.shape[0], input_shape)
        x_train = x_train.astype('float32')
        print('Label Shape: ' + str(y_train.shape))
        print('Training Data Shape: ' + str(x_train.shape))
        model = cnn_model_1h(input_shape, num_sensors, num_classes, time_step)
    else:
        num_time_periods, num_sensors = x_train_a.shape[1], x_train_a.shape[2]
        input_shape = num_time_periods*num_sensors
        x_train_a = x_train_a.reshape(x_train_a.shape[0], input_shape)
        x_train_a = x_train_a.astype('float32')
        x_train_g = x_train_g.reshape(x_train_g.shape[0], input_shape)
        x_train_g = x_train_g.astype('float32')
        print('Label Shape: ' + str(y_train.shape))
        if type == '1H-A' or type == '1H-G':
            print('Training Data Shape: ' + str(x_train_a.shape))
            model = cnn_model_1h(input_shape, num_sensors, num_classes, time_step)
        else:
            print('Training Data Shape: ' + str(x_train_a.shape) + ' ,' + str(x_train_a.shape))
            model = cnn_model_2h(input_shape, num_sensors, num_classes, time_step)

    model.summary()
    model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
    callbacks=[ModelCheckpoint(filepath=tr_model_path, monitor='val_loss', save_best_only=True)]
    if type == '1H':
        history = model.fit(x_train, y_train, batch_size = 32, epochs = 20, callbacks=callbacks, validation_split=0.2, verbose=1)
    elif type == '1H-A':
        history = model.fit(x_train_a, y_train, batch_size = 32, epochs = 20, callbacks=callbacks, validation_split=0.2, verbose=1)
    elif type == '1H-G':
        history = model.fit(x_train_g, y_train, batch_size = 32, epochs = 20, callbacks=callbacks, validation_split=0.2, verbose=1)
    else:
        history = model.fit([x_train_a, x_train_g], y_train, batch_size = 32, epochs = 20, callbacks=callbacks, validation_split=0.2, verbose=1)
    model = tf.keras.models.load_model(tr_model_path)
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    tflite_model = converter.convert()
    open(trl_model_path, 'wb').write(tflite_model)

    model_evaluation(tr_model_path, data_path, time_step, type)
    print('[INFO] Models are saved in ' + result_path)