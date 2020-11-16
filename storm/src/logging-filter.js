process.stdin.resume();
process.stdin.setEncoding('utf8');

var lingeringLine = "";

process.stdin.on('data', function(chunk) {
    lines = chunk.split("\n");

    lines[0] = lingeringLine + lines[0];
    lingeringLine = lines.pop();

    lines.forEach(processLine);
});

process.stdin.on('end', function() {
    processLine(lingeringLine);
});

const names = [];

function time(){
    const date = new Date();
    return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}.${(date.getMilliseconds())}`;
}
function pad(str, length=2, filler="0", pre=false){
    return (pre?str:"")+Array(Math.max(0,length - str.toString().length)).fill(filler).join("")+((!pre)?str:"");
}
function processLine(x){
    if(x.startsWith("LOG|")){
        const parts = x.split("|");
        const name = parts[1];
        const msg = parts[2];
        if(names.indexOf(name) === -1){
            names.push(name);
        }
        const color = 1 + names.indexOf(name) % 7;
        console.log(time()+" \u001b[4"+color+"m\u001b[30m\u001b[0m"+pad(name,25," ", true)+"\u001b[0m "+msg+"\u001b[0m");
    }else if(x.match("BoltExecutor - ")){
        console.log(x.split("BoltExecutor - ")[1]);
    }else if(x.match("Shutting down master")){
        console.log("Shutting down.");
        process.exit();
    }
}
