====
package com.scoreloop.client.android.core.model

----
Image.java (Added)

public byte[] toUserImagePng()

----
Continuation.java (Added)

public void withValue(T value, Exception error)

----
Achievement.java

Added:
public void incrementValue()

----
User.java

Added:
public void assignImage(final Image image, final Continuation<Boolean> contination)

====
package com.scoreloop.client.android.core.addon

----
AndroidImage.java (Added)

protected byte[] toPngByteArray(final int maxWidth, final int maxHeight, final ResizeMode resizeMode)

====eof