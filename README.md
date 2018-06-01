#### android 端activity滑动返回的控件

> 将上面两个类的拷贝到项目中，drawable下的一张.9也拷贝到项目中的drawable下

1.在application中初始化ActivityStackUtil
```
  ActivityStackUtil.getInstance().init(this);
```
  
  
2.给需要滑动返回的activity的(style.xml)theme添加如下两行代码
```
   <item name="android:windowIsTranslucent">true</item>
   <item name="android:windowBackground">@android:color/transparent</item>
```


//3.在需要滑动返回的activity的onCreate()方法中调用
```
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    //在 super.onCreate(savedInstanceState);之前调用此方法
  //第二个参数是一个滑动的监听，一般情况下设置为null即可
        new SlideBackLayout(this).attach2Activity(this, null);
        super.onCreate(savedInstanceState);
    }
```
