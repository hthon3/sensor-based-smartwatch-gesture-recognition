import os
import numpy as np
import pandas as pd
import glob
from matplotlib import pyplot as plt
import matplotlib.gridspec as gridspec
from scipy.signal import lfilter
from scipy.interpolate import CubicSpline 

def gen_random_curve(data, sigma, knot):
    xx = (np.ones((data.shape[1],1))*(np.arange(0,data.shape[0], (data.shape[0]-1)/(knot+1)))).transpose()
    yy = np.random.normal(loc=1.0, scale=sigma, size=(knot+2, data.shape[1]))
    x_range = np.arange(data.shape[0])
    cs_ax = CubicSpline(xx[:,0], yy[:,0])
    cs_ay = CubicSpline(xx[:,1], yy[:,1])
    cs_az = CubicSpline(xx[:,2], yy[:,2])
    return np.array([cs_ax(x_range),cs_ay(x_range),cs_az(x_range)]).transpose()

def distort_time_steps(data):
    tt = gen_random_curve(data, 0.3, 4)
    tt_cum = np.cumsum(tt, axis=0)
    t_scale = [(data.shape[0]-1)/tt_cum[-1,0],(data.shape[0]-1)/tt_cum[-1,1],(data.shape[0]-1)/tt_cum[-1,2]]
    tt_cum[:,0] = tt_cum[:,0] * t_scale[0]
    tt_cum[:,1] = tt_cum[:,1] * t_scale[1]
    tt_cum[:,2] = tt_cum[:,2] * t_scale[2]
    return tt_cum


def scaling(data, sigma = 0.15):
    scalingFactor = np.random.normal(loc=1.0, scale=sigma, size=(1,data.shape[1]))
    myNoise = np.matmul(np.ones((data.shape[0],1)), scalingFactor)
    #plot_flow(data, myNoise, data * myNoise, 'Data Scaling Process')
    return data * myNoise

def magnitude_warping(data):
    myNoise = gen_random_curve(data, 0.15, 3)
    #plot_flow(data, myNoise, data * myNoise, 'Magnitude Warping Process')
    return data * myNoise

def time_warping(data_A, data_G):
    tt_new = distort_time_steps(data_A)
    data_new_A = np.zeros(data_A.shape)
    data_new_G = np.zeros(data_G.shape)
    x_range = np.arange(data_A.shape[0])
    data_new_A = pd.DataFrame(data_new_A)
    data_new_G = pd.DataFrame(data_new_G)    
    data_new_A.iloc[:,0] = np.interp(x_range, tt_new[:, 0], data_A.iloc[:,0])
    data_new_A.iloc[:,1] = np.interp(x_range, tt_new[:, 0], data_A.iloc[:,1])
    data_new_A.iloc[:,2] = np.interp(x_range, tt_new[:, 0], data_A.iloc[:,2])
    data_new_G.iloc[:,0] = np.interp(x_range, tt_new[:, 0], data_G.iloc[:,0])
    data_new_G.iloc[:,1] = np.interp(x_range, tt_new[:, 0], data_G.iloc[:,1])
    data_new_G.iloc[:,2] = np.interp(x_range, tt_new[:, 0], data_G.iloc[:,2])
    #plot_flow(data_A, tt_new[:, 0], data_new_A, 'Time Warping Process')
    return data_new_A, data_new_G

def split(data):
    return data.loc[:, ['accel_x','accel_y','accel_z']], data.loc[:, ['gyro_x','gyro_y','gyro_z']] 

def noise_remove(data):
    n = 5
    b = [1.0 / n] * n
    a = 1
    yy = lfilter(b,a, data)
    return yy

def resampling(data, time_step):
    new_data = data.copy()
    cutoff = new_data.shape[0] - time_step
    new_data = new_data[cutoff:].reset_index(drop=True)
    return new_data

def plot_axis(ax, x, y0, y1, y2, title):
    ax.plot(x, y0, label='X-Axis')
    ax.plot(x, y1, label='Y-Axis')
    ax.plot(x, y2, label='Z-Axis')
    ax.set_title(title)
    ax.set_xlim([min(x), max(x)])
    ax.grid(True)

def plot_activity(activity, data):
    fig, (ax0, ax1) = plt.subplots(nrows=2, figsize=(15, 10), sharex=True)
    data['time'] = range(0, len(data))
    plot_axis(ax0, data['time'], data['accel_x'], data['accel_y'], data['accel_z'], 'Acceleromoter')
    plot_axis(ax1, data['time'], data['gyro_x'], data['gyro_y'], data['gyro_z'], 'Gyroscope')
    fig.suptitle(activity)
    plt.show()

def plot_flow(data, modifier, result, name):
    fig, ((ax1, ax3), (ax1, ax2)) = plt.subplots(2, 2, figsize=(15, 10))
    fig.suptitle(name)
    ax1.plot(data)
    ax2.plot(result)
    ax3.plot(modifier)
    plt.show()

def print_all_data(activity, data):
    for fr in range(0, data[data['activity'] == activity]['activity'].count() - 200 + 1, 200):
        plot_activity(activity, data[data['activity'] == activity][fr: (fr + 200)])

def data_preprocess(path, result_path, result_file_path, time_step, augment = True):
    column_names = ['accel_x','accel_y','accel_z','gyro_x','gyro_y','gyro_z','activity']
    all_folders = glob.glob(path + '/**')
    li = []
    counter = 0
    for folder in all_folders:    
        if folder != result_path:
            all_files = glob.glob(folder + '/*.csv')
            for file in all_files:
                df = pd.read_csv(file, names = column_names, header=None)
                df['userid'] = counter
                df = resampling(df, time_step)            
                if df.shape[0] != time_step:
                    print(file + ': Row Number Error')
                    break
                li.append(df)
                if augment and counter != len(all_folders) - 2:
                    myA, myG = split(df)
                    df_1 = pd.concat([scaling(myA),scaling(myG)], axis= 1, ignore_index=True)
                    df_2 = pd.concat([magnitude_warping(myA),magnitude_warping(myG)], axis= 1, ignore_index=True)
                    tw_a, tw_g = time_warping(myA, myG)
                    df_3 = pd.concat([tw_a, tw_g], axis= 1, ignore_index=True)
                    df_1['activity'] = df_2['activity'] = df_3['activity'] = df['activity']
                    df_1.columns = df_2.columns = df_3.columns = column_names            
                    df_1['userid'] = df_2['userid'] =  df_3['userid'] = df['userid']
                    li.append(df_1)
                    li.append(df_2)
                    li.append(df_3)
            counter = counter + 1
    frame = pd.concat(li, ignore_index=True)
    print('[INFO] Label Number: ' + str(frame['activity'].nunique()))
    for act in frame['activity'].unique():
        print(str(frame[frame['activity'] == act]['activity'].count() / time_step) + ' Samples (Gesture Label: ' + act + ')')
    frame.to_csv(result_file_path, header=False, index=False)
    print('[INFO] Preprocessed data are saved in ' + result_file_path + ' (Total samples: ' + str(frame.shape[0] / time_step) + ')')