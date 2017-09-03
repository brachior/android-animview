Android AnimView
================

[![Release](https://img.shields.io/badge/latest%20release-1.0.2-green.svg)](https://github.com/brachior/android-animview/releases/tag/v1.0.2)
[![Download](https://api.bintray.com/packages/brachior/android/animview/images/download.svg)](https://bintray.com/brachior/android/animview/_latestVersion)
[![License](http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png)](http://www.wtfpl.net/)

![demo](https://raw.githubusercontent.com/brachior/android-animview/master/demo.gif)

### Usage

Add this to your app build.gradle:

```gradle
dependencies {
    compile 'net.brach.android:animview:1.0.2'
}
```

##### ➫ XML

```xml
<!-- default values -->
<net.brach.android.animview.TextAnimView
    android:id="@+id/loading"
    app:tva_text="Loading..."
    app:tva_textSize="10sp"
    app:tva_textColor="@android:color/black"
    app:tva_textAllCaps="false"
    app:tva_textStyle="normal"  // (normal, bold, italic, italic_bold)
    app:tva_typeface="normal"  // (normal, sans, serif, monospace)
    app:tva_fontFamily="null"
    app:tva_text_animation_duration="300"  // animation on text change or/and text color change
    app:tva_bounce_effect="10dp"  // bounce distance
    app:tva_bounce_duration="100"  // bounce duration
    app:tva_bounce_reload_duration="500"  // time before animation restart
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

##### ➫ JAVA

You can start and stop the animation calling the _.start()_ or the _.stop()_ methods.

```java
final TextAnimView anim = (TextAnimView) findViewById(R.id.loading);
anim.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if (toggleAnim) {
            anim.stop();
        } else {
            anim.start();
        }
        toggleAnim = !toggleAnim;
    }
});
```

### Contribution

Feel free to create pull requests / issues

### Licence

This project is licensed under the WTFPL (Do What The Fuck You Want To Public License, Version 2)

[![WTFPL](http://www.wtfpl.net/wp-content/uploads/2012/12/logo-220x1601.png)](http://www.wtfpl.net/)

Copyright © 2017 brachior [brachior@gmail.com](mailto:brachior@gmail.com)

This work is free. You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by Sam Hocevar. See the COPYING file for more details.
