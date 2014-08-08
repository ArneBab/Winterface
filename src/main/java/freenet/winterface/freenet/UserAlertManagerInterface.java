package freenet.winterface.freenet;

import java.util.ArrayList;
import java.util.List;

import freenet.node.useralerts.UserAlert;
import freenet.node.useralerts.UserAlertManager;

public class UserAlertManagerInterface {
	
	private final UserAlertManager uam;

	public UserAlertManagerInterface(UserAlertManager uam) {
		this.uam = uam;
	}
	
	public UserAlert[] getAlerts() {
		return uam.getAlerts();
	}
	
	public UserAlert[] getValidAlerts() {
		List<UserAlert> validAlertstList = new ArrayList<UserAlert>();
		for (UserAlert alert : getAlerts()) {
			if(alert.isValid())
				validAlertstList.add(alert);
		}
		return validAlertstList.toArray(new UserAlert[validAlertstList.size()]);
	}
	
	public int getValidAlertCount() {
		int count = 0;
		for (UserAlert ua : getAlerts()) {
			if(ua.isValid()) {
				count++;
			}
		}
		return count;
	}
	
	public void dismissAlert(int alertHashCode) {
		uam.dismissAlert(alertHashCode);
	}
	
	public int getAlertAnchorSafe(String anchorUnsafe) {
		String[] anchorSubstrings = anchorUnsafe.split(":");
		return Integer.parseInt(anchorSubstrings[anchorSubstrings.length - 1]);
	}

	public int alertClass(UserAlert alert) {
		return alert.getPriorityClass();
	}
	
	public int alertsHighestClass() {
		if (getValidAlerts().length > 0)
			return getValidAlerts()[0].getPriorityClass();
		else
			return UserAlert.MINOR + 1;
	}

}