class User {
  private String name;
  
  User(String name) {
    this.name = name;
  }
  
  void setName(String name) {
    this.name = name;
  }
  
  void saveUser() {
    PrintWriter userFile = createWriter("data/users/" + this.name + ".user.tetris");
    userFile.flush();
    userFile.close();
  }
}
