/*
 * Copyright 2012 GREE, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gree.asdk.core.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.InputStreamEntity;

public class ChunkedInputStreamEntity extends InputStreamEntity {
  private final static int BUFFER_SIZE = 2048;
  private boolean consumed = false;

  public ChunkedInputStreamEntity(InputStream instream, long length) {
    super(instream, length);
  }

  @Override
  public void writeTo(final OutputStream outstream) throws IOException {
    if (outstream == null) { throw new IllegalArgumentException("Output stream may not be null"); }
    InputStream instream = this.getContent();
    byte[] buffer = new byte[BUFFER_SIZE];
    int l;
    if (this.getContentLength() < 0) {
      // consume until EOF
      while ((l = instream.read(buffer)) != -1) {
        outstream.write(buffer, 0, l);
      }
    } else {
      // consume no more than length
      long remaining = this.getContentLength();
      while (remaining > 0) {
        l = instream.read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
        if (l == -1) {
          break;
        }
        outstream.write(buffer, 0, l);
        remaining -= l;
      }
    }
    this.consumed = true;
  }
  
  public boolean isStreaming() {
    return !this.consumed;
  }
  
  public void consumeContent() throws IOException {
    super.consumeContent();
  }
}
