echo "start sending push notification"

curl -v --header 'appkey:pushdemo' -d msg='{"title":"you got new message","conte
nt":"push test content"}' http://210.76.97.31:7894/event/pushdemo

echo "push notification sent"

sleep 10

echo "start sending push message"
curl -v --header 'appkey:pushdemo' -d msg='{"message":"push msg passthrough"}' http://210.76.97.31:7894/event/pushdemo

echo "push message sent"

