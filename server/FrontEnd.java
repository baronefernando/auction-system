import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class FrontEnd implements Auction
{
    List<Integer> ReplicaIDs = new ArrayList<Integer>();

    public FrontEnd()
    {
        super();
        updateReplicaPool();
        setPrimaryReplica();
    }

    //This method will update the alive replicas pool
    private void updateReplicaPool()
    {
        System.out.println("\nReplica pool updating...");
        List<Integer> nonFunctionalIDs = new ArrayList<Integer>();

        try
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            String[] serverNames = registry.list();

            //Separates ID from rmi name
            for (int i = 0; i < serverNames.length; i++) 
            {
                if(serverNames[i].contains("Auction"))
                {   
                    String extractID = serverNames[i];
                    extractID = extractID.replaceAll("\\D+","");
                    if(ReplicaIDs.contains(Integer.parseInt(extractID)) == false)
                    {
                        ReplicaIDs.add(Integer.parseInt(extractID));
                    }
                }
            }

            //Checks which replica is alive
            for (int i = 0; i < ReplicaIDs.size(); i++) 
            {
                try
                {
                    ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + ReplicaIDs.get(i));
                    server.getPrimaryStatus();
                } 
                catch(Exception e)
                {
                    System.out.println("---------------------------------------------------------------------------------------------");
                    System.out.println("INFO: Replica ID " + ReplicaIDs.get(i) +  "\nSTATUS: Not responding. Removed from alive pool.");
                    System.out.println("---------------------------------------------------------------------------------------------");

                    nonFunctionalIDs.add(ReplicaIDs.get(i));
                    registry.unbind("Auction" + ReplicaIDs.get(i));
                }
            }

            //Removes ID's from replicas not responding
            for (int i = 0; i < nonFunctionalIDs.size(); i++) 
            {
                ReplicaIDs.remove(nonFunctionalIDs.get(i));
            }
        }
        catch(Exception e)
        {
            System.err.println("No servers available to process this request.");
            return;
        }

        System.out.println("Replica pool updated successfully!\n");

        //Display alive replicas and primary replica ID
        if(ReplicaIDs.size() > 0)
        {
            System.out.print("Alive Replicas IDs: ");
            for(int i = 0; i < ReplicaIDs.size(); i++) 
            {
                if(i == (ReplicaIDs.size()-1) || ReplicaIDs.size() < 2)
                {
                    System.out.print(ReplicaIDs.get(i));
                    break;
                }
                System.out.print(ReplicaIDs.get(i) + ", ");
            }
            
            System.out.println("\nPrimary Replica ID: " + getPrimaryReplicaID());
            System.out.println("\n");
        }
        else
        {
            System.out.println("No replicas available.\n");
        }
    }

    //This method will check if a primary replica is existent if not it will set the lowest ID as the primary replica.
    private void setPrimaryReplica()
    {
        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            for (int i = 0; i < ReplicaIDs.size(); i++) 
            {
                registry = LocateRegistry.getRegistry("localhost");
                ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + ReplicaIDs.get(i));

                if(server.getPrimaryStatus() == true)
                {
                    return;
                }
            }

            if(ReplicaIDs.size() > 0)
            {
                ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + Collections.min(ReplicaIDs));
                server.updatePrimaryStatus(true); 
            }
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }
    }

    //This method will foward the newUser request to the primary replica.
    public NewUserInfo newUser(String email)
    {
        updateReplicaPool();
        setPrimaryReplica();
        int primaryReplica = getPrimaryReplicaID();

        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + primaryReplica);

            server.updateReplicaPool(ReplicaIDs);
            NewUserInfo user = server.newUser(email);
            return user;   
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }

        return null;
    }

    //This method will foward the getSpec request to the primary replica.
    public AuctionItem getSpec(int itemID)
    {
        updateReplicaPool();
        setPrimaryReplica();
        int primaryReplica = getPrimaryReplicaID();

        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + primaryReplica);
            
            server.updateReplicaPool(ReplicaIDs);
            AuctionItem item = server.getSpec(itemID);
            return item;   
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }

        return null;
    }

    //This method will foward the newAuction request to the primary replica.
    public int newAuction(int userID, AuctionSaleItem item)
    {
        updateReplicaPool();
        setPrimaryReplica();
        int primaryReplica = getPrimaryReplicaID();

        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + primaryReplica);
            
            server.updateReplicaPool(ReplicaIDs);
            int result = server.newAuction(userID,item);
            return result;   
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }

        return 0;
    }

    //This method will foward the listItems request to the primary replica.
    public AuctionItem[] listItems()
    {
        updateReplicaPool();
        setPrimaryReplica();
        int primaryReplica = getPrimaryReplicaID();

        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + primaryReplica);
            
            server.updateReplicaPool(ReplicaIDs);
            AuctionItem[] result = server.listItems();
            return result;   
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }

        return null;
    }

    //This method will foward the closeAuction request to the primary replica.
    public AuctionCloseInfo closeAuction(int userID, int itemID) 
    {
        updateReplicaPool();
        setPrimaryReplica();
        int primaryReplica = getPrimaryReplicaID();

        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + primaryReplica);
            
            server.updateReplicaPool(ReplicaIDs);
            AuctionCloseInfo result = server.closeAuction(userID,itemID);
            return result;   
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }

        return null;
    }

    //This method will foward the bid request to the primary replica.
    public boolean bid(int userID, int itemID, int price)
    {
        updateReplicaPool();
        setPrimaryReplica();
        int primaryReplica = getPrimaryReplicaID();

        try 
        {
            Registry registry = LocateRegistry.getRegistry("localhost");
            ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + primaryReplica);
            
            server.updateReplicaPool(ReplicaIDs);
            boolean result = server.bid(userID,itemID,price);
            return result;   
        } 
        catch (Exception e) 
        {
            System.err.println("No servers available to process this request.");
        }

        return false;
    }

    //This method return the primary replica ID.
    public int getPrimaryReplicaID()
    {
        setPrimaryReplica();
        for (int i = 0; i < ReplicaIDs.size(); i++) 
        {
            try 
            {
                Registry registry = LocateRegistry.getRegistry("localhost");
                ReplicaInterface server = (ReplicaInterface) registry.lookup("Auction" + ReplicaIDs.get(i));

                if(server.getPrimaryStatus() == true)
                {
                    return ReplicaIDs.get(i);
                }
            } 
            catch (Exception e) 
            {
                System.err.println("No servers available to process this request.");
            }
        }
        return -1;
    }

    public static void main(String[] Args)
    {
        try
        {
            FrontEnd f = new FrontEnd();
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(f, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
    
            System.out.println("Front-end is running...");
            System.out.println("\nPress [Ctrl] + [C] to exit.");
        }
        catch(Exception e)
        {
            System.err.println("Exception when starting server occurred. Please try again.");
        }
        
    }

    public byte[] challenge(int userID){return null;}
    public boolean authenticate(int userID, byte signature[]) {return false;}
}
