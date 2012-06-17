package net.gree.asdk.core.request;

import net.gree.asdk.core.Core;
import net.gree.asdk.core.auth.AuthorizerCore;


public class FailureResponse {
  public static final int ERROR_CODE_GENERAL = 1001;
  public static final int ERROR_CODE_INVALID_ACCESS_TOKEN = 1002;
  public static final int ERROR_CODE_NOT_ENOUGH_PRIVILEGES = 1003;
  public static final int ERROR_CODE_OAUTH_UNKOWN = 2000;
  public static final int ERROR_CODE_OAUTH_FAILED = 2001;
  public static final int ERROR_CODE_OAUTH_INVALID_APPLICATION = 2002;
  public static final int ERROR_CODE_OAUTH_INVALID_TIMESTAMP = 2003;

  public static final int SHOW_DIALOG = 1;

  private int code;
  private String message;
  private String ref_url;
  private int[] __error;

  public int getCode() { return code; }
  public String getMessage() { return message; }
  public String getRefUrl() { return ref_url; }
  public int[] getError() { return __error; }

  public boolean handleError() {
    switch (code) {
      case ERROR_CODE_GENERAL:
        break;
      case ERROR_CODE_INVALID_ACCESS_TOKEN:
        if (needDialog()) {
          AuthorizerCore.getInstance().reauthorize(Core.getInstance().getContext(), null);
        }
        return true;
      case ERROR_CODE_NOT_ENOUGH_PRIVILEGES:
        if (2 <= __error.length && needDialog()) {
          AuthorizerCore.getInstance().upgrade(Core.getInstance().getContext(), getTargetGrade(), null, null, null);
        }
        return true;
      default:
        break;
    }
    return false;
  }

  boolean needDialog() {
    return 0 < __error.length && __error[0] == SHOW_DIALOG;
  }

  int getTargetGrade() {
    if (2 <= __error.length) return __error[1];
    return 0;
  }
}
