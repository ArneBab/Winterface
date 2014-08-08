package freenet.winterface.freenet;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;

import java.util.List;

import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.clients.http.bookmark.BookmarkCategory;
import freenet.clients.http.bookmark.BookmarkItem;
import freenet.keys.FreenetURI;
import freenet.node.DarknetPeerNode;
import freenet.node.FSParseException;
import freenet.node.Node;
import freenet.node.NodeStarter;
import freenet.node.PeerManager;
import freenet.node.Version;
import freenet.node.SecurityLevels.NETWORK_THREAT_LEVEL;
import freenet.node.SecurityLevels.PHYSICAL_THREAT_LEVEL;
import freenet.node.useralerts.UserAlert;
import freenet.support.SimpleFieldSet;
import freenet.winterface.core.HighLevelSimpleClientInterface;
import freenet.winterface.freenet.BookmarkFreenetInterface.BookmarkCategoryWithPath;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;

/**
 * {@link FreenetInterface} implementation that uses a {@link Node} instance.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NodeFreenetInterface implements FreenetInterface {

	private final Node node;
	private final PeerManager peerManager;

	private final UserAlertManagerInterface uamInterface;
	private final BookmarkFreenetInterface bmInterface;

	public NodeFreenetInterface(Node node) {
		this(node, node.peers, new BookmarkFreenetInterface(node), new UserAlertManagerInterface(node.clientCore.alerts));
	}

	@VisibleForTesting
	NodeFreenetInterface(Node node, PeerManager peerManager, BookmarkFreenetInterface bmInterface, UserAlertManagerInterface uamInterface) {
		//TODO get winterface plugin instance, substitute the one put in context
		this.node = node;
		this.peerManager = peerManager;
		this.bmInterface = bmInterface;
		this.uamInterface = uamInterface;
	}
	
	@Override
	public String publicVersion() {
		return Version.publicVersion();
	}
	
	@Override
	public int buildNumber() {
		return Version.buildNumber();
	}
	
	@Override
	public String fredMinVersionSupported() {
		return "0.7.5";
	}
	
	@Override
	public int fredMinBuildSupported() {
		return 1465;
	}
	
	//FIXME Should we check for this every time we load a page?
	// Alternative polish: Replace with a method in Routes (plugin initialization), create a template 
	// "version not supported" and route all requests there (bind that VelocityBase servlet to *)
	// Also update the methods fredMin*Supported to use that value
	@Override
	public boolean isFredVersionSupported() {
		String[] version = publicVersion().split("\\.");
		String[] minVersion = fredMinVersionSupported().split("\\.");
		for (int i = 0; i < minVersion.length; i++) {
			try {
				if (Integer.parseInt(version[i]) > Integer.parseInt(minVersion[i]))
					return true;
			} catch (NumberFormatException nfe) {
				//TODO Log exception
				nfe.printStackTrace();
				return false;
			}
		}
		if (buildNumber() >= fredMinBuildSupported()) {
			return true;
		}
		return false;
	}
	
	@Override
	public String cvsRevision() {
		return Version.cvsRevision();
	}
	
	@Override
	public String extRevisionNumber() {
		return NodeStarter.extRevisionNumber;
	}

	@Override
	public ConnectionOverview getConnections() {
		int numberOfCurrentConnections = getNumberOfCurrentConnections();
		int numberOfMaximumConnections = getNumberOfMaximumConnections();
		return new ConnectionOverview(numberOfCurrentConnections, numberOfMaximumConnections);
	}

	private int getNumberOfCurrentConnections() {
		return peerManager.countConnectedPeers();
	}

	private int getNumberOfMaximumConnections() {
		if (node.isOpennetEnabled()) {
			return node.getOpennet().getNumberOfConnectedPeersToAimIncludingDarknet();
		}
		return from(asList(peerManager.getDarknetPeers())).filter(notNull()).filter(enabledPeers()).size();
	}

	private Predicate<? super DarknetPeerNode> enabledPeers() {
		return new Predicate<DarknetPeerNode>() {
			@Override
			public boolean apply(DarknetPeerNode darknetPeerNode) {
				return !darknetPeerNode.isDisabled();
			}
		};
	}
	
	@Override
	public NETWORK_THREAT_LEVEL getNetworkThreatLevel() {
		return node.securityLevels.getNetworkThreatLevel();
	}
	
	@Override
	public PHYSICAL_THREAT_LEVEL getPhysicalThreatLevel() {
		return node.securityLevels.getPhysicalThreatLevel();
	}
	
	@Override
	public int getFproxyPort() {
		SimpleFieldSet fproxyConfig = node.config.get("fproxy").exportFieldSet(true);
		try {
			return fproxyConfig.getInt("port");
		} catch (FSParseException e) {
			e.printStackTrace();
			return 8888;
		}
	}

	@Override
	public FetchResult fetchURI(FreenetURI uri) throws FetchException {
		return HighLevelSimpleClientInterface.fetchURI(uri);
	}

	@Override
	public List<BookmarkCategoryWithPath> getBookmarkCategories() {
		return bmInterface.getBookmarkCategories();
	}
	
	@Override
	public int getBookmarkCategoriesCount() {
		return bmInterface.getBookmarkCategoriesCount();
	}

	@Override
	public List<BookmarkItem> getBookmarksFromCat(BookmarkCategory cat) {
		return bmInterface.getBookmarksFromCat(cat);
	}
	
	@Override
	public int getBookmarksFromCatCount(BookmarkCategory cat) {
		return bmInterface.getBookmarksFromCatCount(cat);
	}
	
	@Override
	public BookmarkCategory getCategoryByPath(String path) {
		return bmInterface.getCategoryByPath(path);
	}
	
	@Override
	public String getBookmarkItemPathEncoded(String parentPath, BookmarkItem bmItem) {
		return bmInterface.getBookmarkItemPathEncoded(parentPath, bmItem);
	}

	@Override
	public void removeBookmark(String path) {
		bmInterface.removeBookmark(path);
	}
	
	@Override
	public void moveBookmarkUp(String path, boolean store) {
		bmInterface.moveBookmarkUp(path, store);
	}
	
	@Override
	public void moveBookmarkDown(String path, boolean store) {
		bmInterface.moveBookmarkDown(path, store);
	}
	
	@Override
	public void storeBookmarks() {
		bmInterface.storeBookmarks();
	}
	
	@Override
	public UserAlert[] getAlerts() {
		return uamInterface.getAlerts();
	}
	
	@Override
	public UserAlert[] getValidAlerts() {
		return uamInterface.getValidAlerts();
	}
	
	@Override
	public int getValidAlertCount() {
		return uamInterface.getValidAlertCount();
	}
	
	@Override
	public int alertClass(UserAlert alert) {
		return uamInterface.alertClass(alert);
	}
	
	@Override
	public void dismissAlert(int alertHashCode) {
		uamInterface.dismissAlert(alertHashCode);
	}
	
	@Override
	public int getAlertAnchorSafe(String anchorUnsafe) {
		return uamInterface.getAlertAnchorSafe(anchorUnsafe);
	}
	
	@Override
	public int alertsHighestClass() {
		return uamInterface.alertsHighestClass();
	}
	
}
