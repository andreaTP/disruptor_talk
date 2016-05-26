cd ..
cd wetty
node app.js -p 4000 > /dev/null 2> /dev/null&
cd ../disruptor_talk

screen -d -m -S shared

python -m SimpleHTTPServer > /dev/null 2> /dev/null& 

screen -x shared
