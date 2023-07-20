public class CurrentAuctions implements java.io.Serializable
{
    int itemID;
    int userID;
    int highestBidUserID;

    public CurrentAuctions(int uID, int iID, int hbuID)
    {
        userID = uID;
        itemID = iID;
        highestBidUserID = hbuID;
    }
}