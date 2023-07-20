import java.rmi.RemoteException;
import java.util.List;
import java.rmi.Remote;

public interface ReplicaInterface extends Remote {
  public NewUserInfo newUser(String email) throws RemoteException;
  public byte[] challenge(int userID) throws RemoteException;
  public boolean authenticate(int userID, byte signature[]) throws RemoteException;
  public AuctionItem getSpec(int itemID) throws RemoteException;
  public int newAuction(int userID, AuctionSaleItem item) throws RemoteException;
  public AuctionItem[] listItems() throws RemoteException;
  public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException;
  public boolean bid(int userID, int itemID, int price) throws RemoteException;
  public boolean getPrimaryStatus() throws RemoteException;
  public void updatePrimaryStatus(boolean primaryStatus) throws RemoteException;
  public void updateReplicaPool(List<Integer> allReplicas) throws RemoteException;
  public void updateReplicaUsers(List<String> newUsers) throws RemoteException;
  public void updateReplicaItems(List<AuctionItem> newItems) throws RemoteException;
  public void updateReplicaAuctions(List<CurrentAuctions> newAuctions) throws RemoteException;
  public List<Integer> getReplicaPool() throws RemoteException;
  public List<String> getReplicaUsers() throws RemoteException;
  public List<AuctionItem> getReplicaItems() throws RemoteException;
  public List<CurrentAuctions> getReplicaAuctions() throws RemoteException;
  }