package net.gree.asdk.core.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;

/**
 * This class manages token and secret like KVS with cache file.
 */
class OAuthCache {
  static final String SUB_DIR = "oauth";
  private final File mCacheDir;

  private ReentrantReadWriteLock mLock;

  public OAuthCache(Context context) {
    mCacheDir = context.getCacheDir();
    mLock = new ReentrantReadWriteLock();

  }

  /**
   * Store a pair of token and secret.
   * @param token OAuth token stored as key
   * @param secret OAuth token secret stored as value
   * @throws IOException
   */
  public void save(String token, String secret) throws IOException {
    if (token == null) return;
    FileOutputStream fos = null;
    mLock.writeLock().lock();
    try {
      File file = new File(mCacheDir, SUB_DIR);
      if (!file.exists()) file.mkdir();
      file = new File(mCacheDir, SUB_DIR+"/"+token);
      if (!file.exists()) file.createNewFile();
      fos = new FileOutputStream(file);
      fos.write(secret.getBytes());
    } finally {
      if (fos != null) fos.close();
      mLock.writeLock().unlock();
    }
  }

  /**
   * Retrieve OAuth token secret by relevant OAuth token
   * @param token OAuth token
   * @return OAuth toen secret if it is found, otherwise null
   */
  public String get(String token) {
    String secret = null;
    mLock.readLock().lock();
    try {
      secret = getWithoutLock(token);
    } catch(IOException e) {
    } finally {
      mLock.readLock().unlock();
    }
    return secret;
  }

  private String getWithoutLock(String token) throws IOException {
    String secret = null;
    File file = new File(mCacheDir, SUB_DIR+"/"+token);
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      byte[] readBytes = new byte[fis.available()];
      fis.read(readBytes);
      secret = new String(readBytes);
    } finally {
      if (fis != null) fis.close();
    }
    return secret;
  }

  /**
   * Remove a pair of OAuth token and secret from local cache.
   * @param token OAuth token
   * @return OAuth token secret if it is found, otherwise null
   * @throws IOException
   */
  public String remove(String token) throws IOException {
    String secret = null;
    mLock.writeLock().lock();
    try {
      secret = getWithoutLock(token);
      if (secret != null) {
        File file = new File(mCacheDir, SUB_DIR+"/"+token);
        file.delete();
      }
    } finally {
      mLock.writeLock().unlock();
    }
    return secret;
  }

  /**
   * Clear all the data for OAuth stored in local cache.
   */
  public void clear() {
    mLock.writeLock().lock();
    try {
      File subDir = new File(mCacheDir, SUB_DIR);
      String[] names = subDir.list();
      if (names == null) return;
      for (String name : names) {
        File file = new File(subDir, name);
        file.delete();
      }
      subDir.delete();
    } finally {
      mLock.writeLock().unlock();
    }
  }
}
