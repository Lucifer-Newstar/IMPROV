package woa.app.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Users")
public class UserEntity {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 
 private Long user_id;
 public Long getUser_id() {
	return user_id;
}
 public void setUser_id(Long user_id) {
	this.user_id = user_id;
 }
 public String getUser_firstname() {
	return user_firstname;
 }
 public void setUser_firstname(String user_firstname) {
	this.user_firstname = user_firstname;
 }
 public String getUser_lastname() {
	return user_lastname;
 }
 public void setUser_lastname(String user_lastname) {
	this.user_lastname = user_lastname;
 }
 public String getUser_name() {
	return user_name;
 }
 public void setUser_name(String user_name) {
	this.user_name = user_name;
 }
 public String getUser_email() {
	return user_email;
 }
 public void setUser_email(String user_email) {
	this.user_email = user_email;
 }
 public String getUser_gender() {
	return user_gender;
 }
 public void setUser_gender(String user_gender) {
	this.user_gender = user_gender;
 }
 public Long getUser_height() {
	return user_height;
 }
 public void setUser_height(Long user_height) {
	this.user_height = user_height;
 }
 public Long getUser_weight() {
	return user_weight;
 }
 public void setUser_weight(Long user_weight) {
	this.user_weight = user_weight;
 }
 public String getUser_password() {
	return user_password;
 }
 public void setUser_password(String user_password) {
	this.user_password = user_password;
 }
 
 public UserEntity(Long user_id, String user_firstname, String user_lastname, String user_name, String user_email,
		String user_gender, Long user_height, Long user_weight, String user_password) {
	super();
	this.user_id = user_id;
	this.user_firstname = user_firstname;
	this.user_lastname = user_lastname;
	this.user_name = user_name;
	this.user_email = user_email;
	this.user_gender = user_gender;
	this.user_height = user_height;
	this.user_weight = user_weight;
	this.user_password = user_password;
}
 

 public UserEntity() {
	super();
}


 private String user_firstname;
 private String user_lastname;
 private String user_name;
 private String user_email;
 private String user_gender;
 private Long user_height;
 private Long user_weight;
 private String user_password;
 


}

