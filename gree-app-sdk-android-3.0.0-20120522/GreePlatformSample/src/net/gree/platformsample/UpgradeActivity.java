package net.gree.platformsample;

import net.gree.asdk.api.GreePlatform;
import net.gree.asdk.api.GreeUser;
import net.gree.asdk.api.auth.Authorizer;

import android.os.Bundle;
import android.view.View;
import android.widget.*;


public class UpgradeActivity extends BaseActivity
    implements
      View.OnClickListener,
      Authorizer.UpgradeListener, 
      Authorizer.UpdatedLocalUserListener {
  private final static int GRADE_UNCHECKED = GreeUser.USER_GRADE_LITE;
  private final static int GRADE_2 = GreeUser.USER_GRADE_LIMITED;
  private final static int GRADE_3 = GreeUser.USER_GRADE_STANDARD;

  private TextView currentGradeTextView;
  private RadioGroup gradeRadioGroup;
  private RadioButton gradeRadio2;
  private RadioButton gradeRadio3;
  private Button upgradeButton;
  private Toast toast;
  private int currentGrade;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GreePlatform.activityOnCreate(this, true);
    setCustomizeStyle();
    setContentView(R.layout.upgrade_page);

    currentGradeTextView = (TextView) findViewById(R.id.currentGrade);
    gradeRadio2 = (RadioButton) findViewById(R.id.gradeRadio2);
    gradeRadio3 = (RadioButton) findViewById(R.id.gradeRadio3);
    gradeRadioGroup = (RadioGroup) findViewById(R.id.gradeRadioGroup);
    upgradeButton = (Button) findViewById(R.id.upgradeButton);
    toast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_LONG);

    upgradeButton.setOnClickListener(this);
    setCurrentGrade();
    disableRadiosAndButtonByCurrentGrade();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!tryLoginAndLoadProfilePage()) { return; }
    setUpBackButton();
  }

  @Override
  public void onClick(View v) {
    int checkedRadioButtonId = gradeRadioGroup.getCheckedRadioButtonId();
    switch (checkedRadioButtonId) {
      case GRADE_UNCHECKED:
        toast.setText(R.string.upgrade_validation_error_message);
        toast.show();
        break;
      case R.id.gradeRadio2:
        upgradeTo(GRADE_2);
        break;
      case R.id.gradeRadio3:
        upgradeTo(GRADE_3);
        break;
    }
  }

  @Override
  public void onUpgrade() {}

  @Override
  public void onError() {}

  @Override
  public void onCancel() {}

  @Override
  public void onUpdateLocalUser() {
    setCurrentGrade();
    disableRadiosAndButtonByCurrentGrade();
  }

  @Override
  protected void sync(boolean fromStart) {}

  private void upgradeTo(int grade) {
    Authorizer.upgrade(UpgradeActivity.this, grade, this, this);
  }

  private void setCurrentGrade() {
    GreeUser user = GreePlatform.getLocalUser();
    if (user != null) {
      currentGrade = user.getUserGrade();
      currentGradeTextView.setText(getString(R.string.current_grade_prefix) + " " + currentGrade);
    } else {
      currentGradeTextView.setText("");
    }
  }

  private void disableRadiosAndButtonByCurrentGrade() {
    gradeRadioGroup.clearCheck();
    switch (currentGrade) {
      case GRADE_2:
        gradeRadio2.setEnabled(false);
        gradeRadio2.setTextColor(R.color.gray);
        break;
      case GRADE_3:
        gradeRadio2.setEnabled(false);
        gradeRadio2.setTextColor(R.color.gray);
        gradeRadio3.setEnabled(false);
        gradeRadio3.setTextColor(R.color.gray);
        upgradeButton.setEnabled(false);
        break;
    }
  }

}
