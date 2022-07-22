Parse.Cloud.define("updateFollowers", function(request, response) {
    const objectId = request.params.objectId;
    const followerId = request.params.followerId;
    const following = request.params.following;

    let query = new Parse.Query(Parse.User);
    query.equalTo("objectId", objectId);

    console.log("here!");
    console.log("here..." + objectId + " " + followerId);
    query.first(null, {useMasterKey: true}).then(function(user) {
      console.log("inside query.first...");
      // Retrieved farm
      if(following == "true")
      {
        console.log("adding to followers list...");
        user.add("followers", followerId);
      } else {
        console.log("removing from followers list...");
        user.remove("followers", followerId);
      }
      user.save(null, {useMasterKey: true}).then(function() {
        console.log("inside user save...");
        response.success();
      }, function(error) {
        response.error(error);
      });
      response.success(objectId);
    },
    function(error) {
      console.log("error one...");
      response.error(error);
    });
    });
