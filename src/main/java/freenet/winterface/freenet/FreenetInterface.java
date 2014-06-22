package freenet.winterface.freenet;

/**
 * Defines the interface between Winterface and the Freenet node. All methods
 * in this interface should return as fast as possible.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface FreenetInterface {

	ConnectionOverview getConnections();

}