from flask import Flask, request
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
import flask
from flaskext.mysql import MySQL
import pymysql
from markupsafe import escape

app= Flask(__name__, static_url_path="/")


app.config["JWT_SECRET_KEY"] = "secret_key"  

jwt = JWTManager(app)

mysql = MySQL(app, cursorclass=pymysql.cursors.DictCursor)

app.config["MYSQL_DATABASE_USER"] = "root"
app.config["MYSQL_DATABASE_PASSWORD"] = "14101966"
app.config["MYSQL_DATABASE_DB"] = "androidprojekat"

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

# flask run --host=0.0.0.0




@app.route("/api/register", methods=["POST"])
def register():
    db = mysql.get_db()
    cursor = db.cursor()
    data = request.get_json()

    username = data.get('username')
    password = data.get('password')

    cursor.execute("SELECT * FROM user WHERE username = %s", (username,))
    existing_user = cursor.fetchone()
    if existing_user:
        return flask.jsonify({"error": "Username already exists"}), 400

    cursor.execute("INSERT INTO user (username, password) VALUES (%s, %s)", (username, password))
    db.commit()

    return flask.jsonify({"message": "User registered successfully"}), 201




@app.route("/api/login", methods=["POST"])
@app.route("/api/login", methods=["POST"])
def login():
    db = mysql.get_db()
    cursor = db.cursor()
    login_data = request.get_json()

    username = login_data.get('username')
    password = login_data.get('password')

    cursor.execute("SELECT * FROM user WHERE username = %s", (username,))
    user = cursor.fetchone()

    if not user or not password == user['password']:  
        return flask.jsonify({"msg": "Bad username or password"}), 401

    access_token = create_access_token(identity=user['id']) 
    return flask.jsonify({
        "access_token": access_token,
        "user_id": user['id']  
    }), 200




@app.route("/api/post", methods=["GET"])
def posts():
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT Post.id, Post.Title, Post.Content, User.id, User.username, Topic.title as tema, Topic.id FROM Post inner join User on Post.user_id = User.id inner join post_has_topic on post_has_topic.Post_id=Post.id inner join topic on post_has_topic.Topic_id=Topic.id")
    posts=cursor.fetchall()  

    return flask.jsonify(posts)
  

@app.route("/api/post/<id_post>", methods=["GET"])
def getPost(id_post):
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * from Post inner join User on Post.user_id=User.id where Post.id= %s", (id_post, ))
    post=cursor.fetchone()

    if [post] is None:
        return flask.jsonify(None),404


    return flask.jsonify(post)


@app.route("/api/user/posts", methods=["GET"])
@jwt_required()
def getUserPosts():
    user_id = get_jwt_identity()
    db = mysql.get_db()
    cursor = db.cursor()

    cursor.execute("SELECT Post.id, Post.Title, Post.Content, User.id, User.username FROM Post inner join User on Post.user_id = User.id WHERE User_id = %s", (user_id,))
    posts = cursor.fetchall()

    return flask.jsonify(posts), 200


@app.route("/api/post", methods=["POST"])
@jwt_required()  
def addPost():
    db = mysql.get_db()
    data = request.json  
    title = data.get("title")
    content = data.get("content")
    user_id = get_jwt_identity()

    if not title or not content:
        return flask.jsonify({"error": "Title and content are required"}), 400

    db = mysql.get_db()
    cursor = db.cursor()
    cursor.execute("INSERT INTO Post (title, content, User_id) VALUES (%s, %s, %s)", (title, content, user_id))
    db.commit()

    return flask.jsonify({"message": "Post created successfully", "post_id": cursor.lastrowid}), 201



@app.route("/api/post/<id_post>" , methods=["PUT"])
@jwt_required()
def editPost(id_post):

    editedPost=request.get_json()

    db=mysql.get_db()
    cursor= db.cursor()

    user_id=get_jwt_identity()

    cursor.execute("Select * from Post where id=%s and User_id =%s", (id_post,user_id))
    post=cursor.fetchone()

    print("eeeee" ,user_id)

    if post is None:
        return flask.jsonify({"error" : "Post not found or you do not have permission to edit this post"})

    cursor.execute("update Post set title=%s, content=%s where id=%s", (editedPost['title'],editedPost['content'], id_post))
    db.commit()

    return flask.jsonify(editedPost),200


@app.route("/api/post/<id_post>" , methods=["DELETE"])
@jwt_required()
def deletePost(id_post):
    db=mysql.get_db()
    cursor= db.cursor()

    user_id=get_jwt_identity()


    cursor.execute("Select * from Post where id=%s and User_id=%s", (id_post,user_id))
    post=cursor.fetchone()

    if post is None:
        return flask.jsonify({"error" : "Post not found or you do not have permission to delete this post"})  

    cursor.execute("DELETE from post where id=%s", (id_post,))
    db.commit()

    return flask.jsonify(post),200



@app.route("/api/user", methods=["GET"])
def users():
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * FROM user")
    users=cursor.fetchall()  

    return flask.jsonify(users)
  

@app.route("/api/user/<id_user>", methods=["GET"])
def getUser(id_user):
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * from user where id= %s", (id_user, ))
    user=cursor.fetchone()

    if [user] is None:
        return flask.jsonify(None),404


    return flask.jsonify(user)


@app.route("/api/user", methods=["POST"])
def addUser():
    db= mysql.get_db()
    cursor=db.cursor()
    user= dict(flask.request.json)
    cursor.execute("INSERT INTO user(username,password) values (%s, %s )", (user['username'],user['password']))
    db.commit()

    return flask.jsonify(user),201


@app.route("/api/user/<id_user>" , methods=["PUT"])
def editUser(id_user):
    editedUser=dict(flask.request.json)
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("Select * from user where id=%s", (id_user,))
    user=cursor.fetchone()

    if user is None:
        return flask.jsonify({"error" : "user not found"})

    cursor.execute("update user set username=%s, password=%s where id=%s", (editedUser['username'],editedUser['password'], id_user))
    db.commit()

    return flask.jsonify(editedUser),200


@app.route("/api/user/<id_user>" , methods=["DELETE"])
def deleteUser(id_user):
    db=mysql.get_db()
    cursor= db.cursor()

    cursor.execute("Select * from user where id=%s", (id_user,))
    user=cursor.fetchone()

    if user is None:
        return flask.jsonify({"error" : "user not found"})  

    cursor.execute("DELETE from user where id=%s", (id_user,))
    db.commit()

    return flask.jsonify(user),200  


@app.route("/api/topic", methods=["GET"])
def topics():
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * FROM topic")
    topics=cursor.fetchall()  

    return flask.jsonify(topics)
  

@app.route("/api/topic/<id_topic>", methods=["GET"])
def getTopic(id_topic):
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * from topic where id= %s", (id_topic, ))
    topic=cursor.fetchone()

    if topic is None:
        return flask.jsonify(None),404


    return flask.jsonify(topic)


@app.route("/api/topic", methods=["GET"])
def getTopics():
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * from topic")
    topics=cursor.fetchall()

    if topics is None:
        return flask.jsonify(None),404


    return flask.jsonify(topics)

@app.route("/api/topic", methods=["POST"])
def createTopic():
    db= mysql.get_db()
    cursor=db.cursor()
    topic= dict(flask.request.json)
    cursor.execute("INSERT INTO topic(title) values (%s)", (topic['title']))
    db.commit()

    return flask.jsonify(topic),201


@app.route("/api/topic/<id_topic>" , methods=["PUT"])
def editTopic(id_topic):
    editedTopic=dict(flask.request.json)
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("Select * from topic where id=%s", (id_topic,))
    topic=cursor.fetchone()

    if topic is None:
        return flask.jsonify({"error" : "topic not found"})

    cursor.execute("update topic set title=%s where id=%s", (editedTopic['title'], id_topic))
    db.commit()

    return flask.jsonify(editedTopic),200


@app.route("/api/topic/<id_topic>" , methods=["DELETE"])
def deleteTopic(id_topic):
    db=mysql.get_db()
    cursor= db.cursor()

    cursor.execute("Select * from topic where id=%s", (id_topic,))
    topic=cursor.fetchone()

    if topic is None:
        return flask.jsonify({"error" : "topic not found"})  

    cursor.execute("DELETE from topic where id=%s", (id_topic,))
    db.commit()

    return flask.jsonify(topic),200 








@app.route("/api/post_has_topic/<int:id_post>/<int:id_topic>", methods=["POST"])
def addTopicToPost(id_post, id_topic):
    db = mysql.get_db()
    cursor = db.cursor()



    cursor.execute("INSERT INTO post_has_topic(Post_id, Topic_id) VALUES (%s, %s)", (id_post, id_topic))
    db.commit()

    return flask.jsonify({"message": "Topic added to post", "post_id": id_post, "topic_id": id_topic}), 201


@app.route("/api/post_has_topic/<int:id_post>/<int:id_topic>", methods=["DELETE"])
@jwt_required()
def deleteTopicFromPost(id_post, id_topic):
    db = mysql.get_db()
    cursor = db.cursor()

    user_id = get_jwt_identity()


    cursor.execute("SELECT * FROM Post WHERE id=%s AND User_id=%s", (id_post, user_id))
    post = cursor.fetchone()

    if post is None:
        return jsonify({"error": "Post not found or you do not have permission to edit this post"}), 403


    cursor.execute("DELETE FROM post_has_topic WHERE Post_id = %s AND Topic_id = %s", (id_post, id_topic))
    db.commit()

    return jsonify({"message": "Topic removed from post", "post_id": id_post, "topic_id": id_topic}), 200





@app.route("/api/comment", methods=["GET"])
def comments():
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * FROM comment")
    comments=cursor.fetchall()  
    return flask.jsonify(comments)
  

@app.route("/api/comment/post/<id_post>", methods=["GET"])
def getCommentsFromPost(id_post):
    db = mysql.get_db()
    cursor = db.cursor()
    cursor.execute("SELECT comment.id, comment.content, user.username FROM comment inner join user on comment.User_id=user.id where comment.Post_id=%s",(id_post,))
    comments = cursor.fetchall()
    return flask.jsonify(comments)


@app.route("/api/comment/user/<id_user>", methods=["GET"])
def getCommentsFromUser(id_user):
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * FROM comment where User_id= %s", (id_user, ))
    comments=cursor.fetchall()  
    return flask.jsonify(comments)

@app.route("/api/comment/<id_comment>", methods=["GET"])
def getComment(id_comment):
    db=mysql.get_db()
    cursor= db.cursor()
    cursor.execute("SELECT * from comment where id= %s", (id_comment, ))
    comment=cursor.fetchone()

    if comment is None:
        return flask.jsonify(None),404


    return flask.jsonify(comment)


@app.route("/api/comment/<post_id>", methods=["POST"])
@jwt_required()
def addComment(post_id):
    db= mysql.get_db()
    cursor=db.cursor()
    comment= dict(flask.request.json)
    user_id = get_jwt_identity()
    cursor.execute("INSERT INTO comment(content, User_id, Post_id) values (%s,%s,%s)", (comment['content'], user_id, post_id))
    db.commit()

    return flask.jsonify({
        "content": comment['content'],
        "user_id": user_id,
        "post_id": post_id
    }), 201


@app.route("/api/comment/<id_comment>" , methods=["PUT"])
@jwt_required()
def editComment(id_comment):
    editedComment=dict(flask.request.json)
    db=mysql.get_db()
    cursor= db.cursor()
    user_id = get_jwt_identity()
    cursor.execute("Select * from comment where id=%s AND User_id=%s", (id_comment, user_id))
    comment=cursor.fetchone()

    if comment is None:
        return flask.jsonify({"error" : "comment not found or you do not have permission to edit this comment"})

    cursor.execute("UPDATE comment SET content = %s WHERE id = %s", (editedComment['content'], id_comment))
    db.commit()

    return flask.jsonify(editedComment),200


@app.route("/api/comment/<id_comment>" , methods=["DELETE"])
@jwt_required()
def deleteComment(id_comment):
    db=mysql.get_db()
    cursor= db.cursor()
    user_id = get_jwt_identity()
    cursor.execute("Select * from comment where id=%s AND User_id=%s", (id_comment, user_id))
    comment=cursor.fetchone()

    if comment is None:
        return flask.jsonify({"error" : "comment not found or you do not have permission to edit this comment"})  

    cursor.execute("DELETE from comment where id=%s", (id_comment,))
    db.commit()

    return flask.jsonify(comment),200 

@app.route("/api/posts/topic/<int:topic_id>", methods=["GET"])
def getPostsInTopic(topic_id):
    db = mysql.get_db()
    cursor = db.cursor()
    cursor.execute("SELECT Post.id, Post.Title, Post.Content, User.id, User.username, Topic.title as tema, Topic.id FROM Post inner join User on Post.user_id = User.id inner join post_has_topic on post_has_topic.Post_id=Post.id inner join topic on post_has_topic.Topic_id=Topic.id WHERE Topic.id=%s", (topic_id,))
    posts = cursor.fetchall()
    return flask.jsonify(posts)