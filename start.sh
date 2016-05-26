cd ..
cd wetty
node app.js -p 4000 > /dev/null 2> /dev/null&
cd ../disruptor_talk

python -m SimpleHTTPServer > /dev/null 2> /dev/null& 
