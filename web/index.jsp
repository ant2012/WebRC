<%-- Created by IntelliJ IDEA. --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang=en>
<head>
    <meta charset=utf-8>
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0, maximum-scale=1.0," />
    <title>WebRC :: HTML5 based remote control for mobile robot platform</title>
    <link rel="stylesheet" type="text/css" href="css/main.css">
    <script type="text/javascript" src="js/jquery-1.9.0.min.js"></script>
</head>
<body onload = "init()">
<script src="js/Vector2.js"></script>
<script src="js/ShipMovingTouch.js"></script>
<script src="js/BulletSebs.js"></script>
<script src="js/ServletCommunicator.js"></script>
<script>

var canvas,
        c, // c is the canvas' context 2D
        container,
        halfWidth,
        halfHeight,
        leftTouchID = -1,
        leftTouchPos = new Vector2(0,0),
        leftTouchStartPos = new Vector2(0,0),
        leftVector = new Vector2(0,0),
        servletCommunicator = new ServletCommunicator();


setupCanvas();

var mouseX, mouseY,
// is this running in a touch capable environment?
        touchable = 'createTouch' in document,
        touches = [], // array of touch vectors
        ship = new ShipMoving(halfWidth, halfHeight);
        //bullets = [],
        //spareBullets = [];


document.body.appendChild(ship.canvas);

setInterval(draw, 1000/35);


if(touchable) {
    canvas.addEventListener( 'touchstart', onTouchStart, false );
    canvas.addEventListener( 'touchmove', onTouchMove, false );
    canvas.addEventListener( 'touchend', onTouchEnd, false );
    window.onorientationchange = resetCanvas;
    window.onresize = resetCanvas;
} else {
    canvas.addEventListener( 'mousedown', onMouseDown, false );
    canvas.addEventListener( 'mousemove', onMouseMove, false );
    canvas.addEventListener( 'mouseup'  , onMouseUp  , false );
}

function resetCanvas (e) {
    // resize the canvas - but remember - this clears the canvas too.
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    halfWidth = canvas.width/2;
    halfHeight = canvas.height/2;

    //make sure we scroll to the top left.
    window.scrollTo(0,0);
}

function init(){

}

function draw() {

    c.clearRect(0,0,canvas.width, canvas.height);

    ship.targetVel.copyFrom(leftVector);
    ship.targetVel.multiplyEq(0.2);

    ship.update();

    with(ship.pos) {
        if(x<0) x = canvas.width;
        else if(x>canvas.width) x = 0;
        if(y<0) y = canvas.height;
        else if (y>canvas.height) y = 0;
    }

    //ship.draw();

    /*
    for (var i = 0; i < bullets.length; i++) {
        var bullet = bullets[i];
        if(!bullet.enabled) continue;
        bullet.update();
        bullet.draw(c);
        if(!bullet.enabled)
        {
            spareBullets.push(bullet);

        }


    }
    */

    if(touchable) {

        for(var i=0; i<touches.length; i++) {

            var touch = touches[i];

            if(touch.identifier == leftTouchID){
                c.beginPath();
                c.strokeStyle = "cyan";
                c.lineWidth = 6;
                c.arc(leftTouchStartPos.x, leftTouchStartPos.y, 40,0,Math.PI*2,true);
                c.stroke();
                c.beginPath();
                c.strokeStyle = "cyan";
                c.lineWidth = 2;
                c.arc(leftTouchStartPos.x, leftTouchStartPos.y, 60,0,Math.PI*2,true);
                c.stroke();
                c.beginPath();
                c.strokeStyle = "cyan";
                c.arc(leftTouchPos.x, leftTouchPos.y, 40, 0,Math.PI*2, true);
                c.stroke();

            } else {
                /*
                c.beginPath();
                c.fillStyle = "white";
                c.fillText("touch id : "+touch.identifier+" x:"+touch.clientX+" y:"+touch.clientY, touch.clientX+30, touch.clientY-30);

                c.beginPath();
                c.strokeStyle = "red";
                c.lineWidth = "6";
                c.arc(touch.clientX, touch.clientY, 40, 0, Math.PI*2, true);
                c.stroke();
                */
            }
        }
    } else {

        c.fillStyle	 = "white";
        c.fillText("mouse : "+mouseX+", "+mouseY, mouseX, mouseY);

    }
    //c.fillText("hello", 0,0);

}
/*
function makeBullet() {

    var bullet;

    if(spareBullets.length>0) {

        bullet = spareBullets.pop();
        bullet.reset(ship.pos.x, ship.pos.y, ship.angle);

    } else {

        bullet = new Bullet(ship.pos.x, ship.pos.y, ship.angle);
        bullets.push(bullet);

    }

    bullet.vel.plusEq(ship.vel);


}
*/
/*
 *	Touch event (e) properties :
 *	e.touches: 			Array of touch objects for every finger currently touching the screen
 *	e.targetTouches: 	Array of touch objects for every finger touching the screen that
 *						originally touched down on the DOM object the transmitted the event.
 *	e.changedTouches	Array of touch objects for touches that are changed for this event.
 *						I'm not sure if this would ever be a list of more than one, but would
 *						be bad to assume.
 *
 *	Touch objects :
 *
 *	identifier: An identifying number, unique to each touch event
 *	target: DOM object that broadcast the event
 *	clientX: X coordinate of touch relative to the viewport (excludes scroll offset)
 *	clientY: Y coordinate of touch relative to the viewport (excludes scroll offset)
 *	screenX: Relative to the screen
 *	screenY: Relative to the screen
 *	pageX: Relative to the full page (includes scrolling)
 *	pageY: Relative to the full page (includes scrolling)
 */

function onTouchStart(e) {

    for(var i = 0; i<e.changedTouches.length; i++){
        var touch =e.changedTouches[i];
        //console.log(leftTouchID + " "
        if((leftTouchID<0) && (touch.clientX<halfWidth))
        {
            leftTouchID = touch.identifier;
            leftTouchStartPos.reset(touch.clientX, touch.clientY);
            leftTouchPos.copyFrom(leftTouchStartPos);
            leftVector.reset(0,0);
            continue;
        } else {

            //makeBullet();

        }
    }
    touches = e.touches;
}

function onTouchMove(e) {
    // Prevent the browser from doing its default thing (scroll, zoom)
    e.preventDefault();

    for(var i = 0; i<e.changedTouches.length; i++){
        var touch =e.changedTouches[i];
        if(leftTouchID == touch.identifier)
        {
            leftTouchPos.reset(touch.clientX, touch.clientY);
            leftVector.copyFrom(leftTouchPos);
            leftVector.minusEq(leftTouchStartPos);
            servletCommunicator.sendVector(leftVector);
            break;
        }
    }

    touches = e.touches;

}

function onTouchEnd(e) {

    touches = e.touches;

    for(var i = 0; i<e.changedTouches.length; i++){
        var touch =e.changedTouches[i];
        if(leftTouchID == touch.identifier)
        {
            leftTouchID = -1;
            leftVector.reset(0,0);
            servletCommunicator.sendVector(leftVector);
            break;
        }
    }

}

function onMouseDown(event) {

    var x = event.clientX;
    var y = event.clientX;
    leftVector = new Vector2(0, 0);
    servletCommunicator.sendVector(leftVector);
}

function onMouseMove(event) {

    mouseX = event.offsetX;
    mouseY = event.offsetY;
}

function onMouseUp(event) {

    mouseX = event.offsetX;
    mouseY = event.offsetY;
}

function setupCanvas() {

    canvas = document.createElement( 'canvas' );
    c = canvas.getContext( '2d' );
    container = document.createElement( 'div' );
    container.className = "container";

    document.body.appendChild( container );
    container.appendChild(canvas);

    resetCanvas();

    c.strokeStyle = "#ffffff";
    c.lineWidth =2;
}


</script>
</body>
</html>
