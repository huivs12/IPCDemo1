####**IPC简介**

IPC是Inter-Process Communication的缩写，含义为进程间通信或跨进程通信，指两个进程间进行数据交换的过程。说起进程间通信，首先要理解是什么是进程，什么是线程。线程是CPU调度的最小单位。而进程指一个执行单元，在PC和移动设备上指一个程序或者一个应用。一个进程可以包含多个线程，因此进程和线程是包含与被包含的关系。在Android中的主线程叫作UI线程，在UI线程中才能操作界面元素，很多时候，一个进程需要执行大量耗时的任务，如果这些任务放在主线程中执行的话，就会造成ANR。所以需要把耗时的任务放在子线程中去执行。

Android有自己的进程间通信的方式，就是Binder了。还可以通过Socket实现两个终端的通信或一个设备上的两个进程也可以通过Socket进程通信。
.
.

####**多进程模式**
**如何开启多进程模式：**

通过给四大组件（Activity、Service、Receiver、ContentProvider）在AndroidManifest中指定android:process属性。

通过shell命令查看进程：
adb shell ps 或者 adb shel ps|grep 进程名

```
<activity
            android:name=".MainActivity"
            android:label="@string/app_name" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".SecondActivity"
            android:label="@string/title_activity_second"
            android:process=":remote">
        </activity>
        <activity
            android:name=".ThirdActivity"
            android:label="@string/title_activity_third"
            android:process="com.thh.ipcdemo1.remote">
        </activity>
```

上面的代码可以看出，SecondActivity的android:process的值是以":"开头的，ThirdActivity的android:process则是一个完整的进程名。
以“：”开头的进程属于当前应用的私用进程，其它应用的组件不可以和它跑在同一个进程中，而进程名不以“：”开头的进程属于全局进程，其它应用通过ShareUID方式可以和它跑在同一个进程中。

Android系统会为每个应用分配一个唯一的UID，具有相同UID的应用才能共享数据，两个应用通过ShareUID跑在同一个进程中是有要求的，需要两个应用有相同的ShareUID并且签名相同才可以。在这种情况下，它们可以互相访问对方的私有数据，比如data目录，组件信息等。

（ ShareUID设置在AndroidManifast中manifast标签中的android:sharedUserId属性）
.
.
.
**多进程模式的运行机制**
在上面代码中我们创建了三个Activity，并且指定了他们各自的进程。接下来我们创建一个UserManager类，设置它的静态成员变量mUserId=1;

```
public class UserManager {
    public static int mUserId = 1;
}

```
当在MainActivity中将mUserId的值设置为2，然后分别在每个Activity中输出mUserId的值。
输出结果：
MainActivity mUserId = 2;
SecondActivity mUserId = 1;
ThirdActivity mUserId = 1;

上述问题出现的原因是：由于每个Activity都运行一个单独的进程中，Android为每个应用（进程）分配一个虚拟机，而不同的虚拟机在内存分配上有不同的地址和空间。这就导致不同的虚拟机访问同一个对象会产生多个副本。在一个进程中修改mUserId的值并不会影响到其它进程的mUserId。

所以所有运行在不同进程中的四大组件，只要他们需要内存来共享数据，都会失败。正常情况下，四大组件中间不可能不通过中间层来共享数据。
使用多线程会造成如下方面的问题：
1. 静态成员和单例模式完全失效
2. 线程同步机制完全失效
3. SharedPreferences的可靠性下降
4. Application会多次创建

SharedPreferences底层是通过读写XML来实现的，并发读写是可能出现问题的。
运行在同一个进程的组件是属于同一个虚拟机和同一个Application的，同理，运行在不同进程中的组件是属于两个不同的虚拟机和Application的。

```
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("thh", "[MyApplication onCreate] application start, process name:"+getProcessName());
    }

    private String getProcessName(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo:
            activityManager.getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == android.os.Process.myPid()) {
                return runningAppProcessInfo.processName;
            }
        }
        return null;
    }
}
```

上面的代码去运行，分别启动之前的那三个Activity，会发现Application也会启动三次，分别在不同的进程中。
.
.
.
以上会发现在多进程中所带来的问题有很多，虽说不能直接的共享内存，但是系统还是为我们提供了很多跨进程通信的方式，如通过Intent来传递数据，共享文件和SharedPreferences，基于Binder的Messager 和 AIDL、Socket等等。
