package e.hp.mywhatsapp;

public class Users {

   private String name,image,status,thumb_image,online;

   public Users(){}

    public Users(String name, String image, String status,String thumb_image,String online) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image=thumb_image;
        this.online=online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnline() {
        return online;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public String getStatus() {
        return status;
    }
}
