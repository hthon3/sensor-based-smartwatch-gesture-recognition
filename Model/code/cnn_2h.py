import keras as keras
from keras.models import Sequential
from keras.layers import Dense, Conv1D, Reshape, GlobalAveragePooling1D, Dropout, Input, concatenate, Flatten
from keras.layers.pooling import MaxPool1D

def cnn_model_2h(input_shape, num_sensors, num_classes, time_step):
    #accelerometer Head
    input_accel = Input(shape= (input_shape,))
    reshaped_accel = Reshape((time_step, num_sensors), input_shape=(input_shape,))(input_accel)
    conv1_accel = Conv1D(16, 10, activation='relu', input_shape = (time_step, num_sensors))(reshaped_accel)
    max1_accel = MaxPool1D(2)(conv1_accel)    
    conv2_accel = Conv1D(32, 10, activation='relu')(max1_accel)
    max2_accel = MaxPool1D(2)(conv2_accel)   
    conv3_accel = Conv1D(64, 10, activation='relu')(max2_accel)
    max3_accel = MaxPool1D(2)(conv3_accel) 
    gap_accel = GlobalAveragePooling1D() (max3_accel)
    #Gyroscop Head
    input_gyro = Input(shape=(input_shape,)) 
    reshaped_gyro = Reshape((time_step, num_sensors), input_shape=(input_shape,))(input_gyro)
    conv1_gyro = Conv1D(16, 10, activation='relu', input_shape = (time_step, num_sensors))(reshaped_gyro)
    max1_gyro = MaxPool1D(2)(conv1_gyro)
    conv2_gyro = Conv1D(32, 10, activation='relu')(max1_gyro)
    max2_gyro = MaxPool1D(2)(conv2_gyro)
    conv3_gyro = Conv1D(64, 10, activation='relu')(max2_gyro)
    max3_gyro = MaxPool1D(2)(conv3_gyro)
    gap_gyro = GlobalAveragePooling1D() (max3_gyro)
    #stack
    stacked_features = concatenate([gap_accel, gap_gyro])
    dropout = Dropout(0.5) (stacked_features)
    output = Dense(num_classes, activation='softmax') (dropout)
    model = keras.models.Model(inputs=[input_accel, input_gyro],outputs=[output])
    return model