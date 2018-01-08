@(username: String)(nameGame: String)(numberPlayers: Integer)(numberBots: Integer)

var NUMBER_OF_COLS = 13,
	NUMBER_OF_ROWS = 17,
	BLOCK_SIZE = 100;

var RED = '#FB4357',
	GREEN = '#3EBD1F',
    BLUE = '#5BA3F9';
    YELLOW = '#f2de16';
    ORANGE = '#F4762A';
    BLACK = '#071E22';
    NEUTRAL = '#f8f9fa';
    WARNING = '#ff1717';

var ctx = null,
	json = null,
	canvas = null,
    currentTurn = null,
    currentColor = null,
    currentTextColor = null,
    selectedPiece = null;
    
    function findFieldByS(osR, osC) {
        var curPiece = null,
            iPieceCounter = 0,
            pieceAtBlock = null
            teamOfPieces = json.fields;
    
        for (iPieceCounter = 0; iPieceCounter < teamOfPieces.length; iPieceCounter++) {
    
            curPiece = teamOfPieces[iPieceCounter];
    
            if (curPiece.sCol === osC && curPiece.sRow === osR) {
                curPiece.position = iPieceCounter;
                pieceAtBlock = curPiece;
                iPieceCounter = teamOfPieces.length;
            }
        }
    
        return pieceAtBlock;
    }
    
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
    var chatSocket;
    
    if(@numberPlayers === 0) {
        var chatSocket = new WS("@routes.Application.game(username, nameGame).webSocketURL(request)");
    }
    else {
        var chatSocket = new WS("@routes.Application.gameNew(username, nameGame, numberPlayers, numberBots).webSocketURL(request)");
    }
    
    var nG;
    var us;
    var cl;
    var nP;
    
    function sendMessage(osR, osC, nsR, nsC) {
        chatSocket.send(JSON.stringify(
            {nameOfGame: nG, username: us, color: cl, oldRow: osR, oldCol: osC, newRow: nsR, newCol: nsC}
        ));
    }
    
    function skipTurn() {
        var cT = document.getElementById("turn-info").textContent;
        if(cT.localeCompare(cl) === 0) {
            chatSocket.send(JSON.stringify(
                {nameOfGame: nG, username: us, color: cl, skipTurn: "true"}
            ));
        }
    }
    
        chatSocket.onmessage = function (event) {
            var data = JSON.parse(event.data);
    
            if (data.error) {
                alert(data.error);
                chatSocket.close();
                window.location.href = "/";
            }
            else if(data.name) {
                nG = data.name;
                us = data.user;
                cl = data.color;
                nP = data.numberOfPlayers;
                document.getElementById("player-color").innerHTML = cl;
                document.getElementById("player-color").style.color = getColor(cl);
                document.getElementById("turn-info").innerHTML = "Waiting for players...";
                document.getElementById("turn-info").style.color = WARNING;
            }
            else if(data.kind === "start") {
                drawPieces();
                currentTurn = getTurn(data.turn);
                document.getElementById("turn-info").innerHTML = data.turn;
                document.getElementById("turn-info").style.color = getColor(data.turn);
            }
            else if(data.kind === "skipTurn") {
                currentTurn = getTurn(data.turn);
                document.getElementById("turn-info").innerHTML = data.turn;
                document.getElementById("turn-info").style.color = getColor(data.turn);
            }
            else if(data.kind === "move") {
                var newSBlock = findFieldByS(data.newRow, data.newCol),
                    oldSBlock = findFieldByS(data.oldRow, data.oldCol);
    
                var newBlock = {
                    "row": newSBlock.row,
                    "col": newSBlock.col
                };
    
                var oldBlock = {
                    "row": oldSBlock.row,
                    "col": oldSBlock.col
                };
    
                checkIfPieceClicked(oldBlock);
                movePiece(newBlock);
                currentTurn = getTurn(data.turn);
                document.getElementById("turn-info").innerHTML = data.turn;
                document.getElementById("turn-info").style.color = getColor(data.turn);
            }
            else if(data.kind === "winner") {
                alert("Player " + data.user + " (" + data.turn + ") has all pawns in the correct base!");
            }
        };

function screenToBlock(x, y) {
    var block = null;
    y = y - BLOCK_SIZE / 2;
    
    if(Math.round(y / BLOCK_SIZE) % 2 === 0) {
        block =  {
            "row": Math.round(y / BLOCK_SIZE),
            "col": Math.round((x - BLOCK_SIZE / 2) / BLOCK_SIZE)
        };
    }
    else {
        block =  {
            "row": Math.round(y / BLOCK_SIZE),
            "col": Math.round((x - BLOCK_SIZE) / BLOCK_SIZE)
        };
    }

	return block;
}

function getPieceAtBlockForTeam(teamOfPieces, clickedBlock) {

	var curPiece = null,
		iPieceCounter = 0,
		pieceAtBlock = null;

	for (iPieceCounter = 0; iPieceCounter < teamOfPieces.length; iPieceCounter++) {

		curPiece = teamOfPieces[iPieceCounter];

		if (curPiece.col === clickedBlock.col && curPiece.row === clickedBlock.row) {
            curPiece.position = iPieceCounter;
			pieceAtBlock = curPiece;
			iPieceCounter = teamOfPieces.length;
		}
	}

	return pieceAtBlock;
}

function getBlockColour(iRowCounter, iBlockCounter) {
    var cStartColour;
    
    if(iBlockCounter < 4) {
        cStartColour = RED;
    } 
    else if(iBlockCounter > 12) {
        cStartColour = YELLOW;
    }
    else if(iRowCounter < 8 - iBlockCounter && iBlockCounter >= 4 && iBlockCounter < 6) {
        cStartColour = BLACK;
    }
    else if(iRowCounter < 9 - iBlockCounter && iBlockCounter >= 6 && iBlockCounter < 8) {
        cStartColour = BLACK;
    }
    else if(iRowCounter < iBlockCounter - 7 && iBlockCounter > 8 && iBlockCounter <= 10) {
        cStartColour = ORANGE;
    }
    else if(iRowCounter < iBlockCounter - 8 && iBlockCounter > 10 && iBlockCounter <= 12) {
        cStartColour = ORANGE;
    }
    else if(iRowCounter > iBlockCounter + 4 && iBlockCounter >= 4 && iBlockCounter < 5) {
        cStartColour = GREEN;
    }
    else if(iRowCounter > iBlockCounter + 3 && iBlockCounter >= 5 && iBlockCounter < 7) {
        cStartColour = GREEN;
    }
    else if(iRowCounter > iBlockCounter + 2 && iBlockCounter >= 7 && iBlockCounter < 8) {
        cStartColour = GREEN;
    }
    else if(iRowCounter > iBlockCounter && iBlockCounter > 8 && iBlockCounter <= 9) {
        cStartColour = BLUE;
    }
    else if(iRowCounter > iBlockCounter - 1 && iBlockCounter > 9 && iBlockCounter <= 10) {
        cStartColour = BLUE;
    }
    else if(iRowCounter > iBlockCounter - 3 && iBlockCounter > 10 && iBlockCounter <= 11) {
        cStartColour = BLUE;
    }
    else if(iRowCounter > iBlockCounter - 4 && iBlockCounter > 11 && iBlockCounter <= 12) {
        cStartColour = BLUE;
    }
    else {
        cStartColour = NEUTRAL;
    }

	return cStartColour;
}

function drawBlock(iRowCounter, iBlockCounter) {
    ctx.fillStyle = NEUTRAL;
    
    if(iBlockCounter % 2) {
        ctx.beginPath();
        ctx.arc(iRowCounter * BLOCK_SIZE + BLOCK_SIZE, iBlockCounter * BLOCK_SIZE + BLOCK_SIZE / 2,
            18, 0, 2 * Math.PI, false);
        ctx.fill();
        ctx.stroke();
    }
    else {
        ctx.beginPath();
        ctx.arc(iRowCounter * BLOCK_SIZE + BLOCK_SIZE / 2, iBlockCounter * BLOCK_SIZE + BLOCK_SIZE / 2,
            18, 0, 2 * Math.PI, false);
        ctx.fill();
        ctx.stroke();
    }
    
}

function drawPawn(iRowCounter, iBlockCounter) {
    ctx.fillStyle = getBlockColour(iRowCounter, iBlockCounter);
    
    if(iBlockCounter % 2) {
        ctx.beginPath();
        ctx.arc(iRowCounter * BLOCK_SIZE + BLOCK_SIZE, iBlockCounter * BLOCK_SIZE + BLOCK_SIZE / 2,
            15, 0, 2 * Math.PI, false);
        ctx.fill();
        ctx.stroke();
    }
    else {
        ctx.beginPath();
        ctx.arc(iRowCounter * BLOCK_SIZE + BLOCK_SIZE / 2, iBlockCounter * BLOCK_SIZE + BLOCK_SIZE / 2,
            15, 0, 2 * Math.PI, false);
        ctx.fill();
        ctx.stroke();
    }
}

function drawPiece(curPiece) {
    drawPawn(curPiece.col, curPiece.row);
}

function drawTeamOfPieces(teamOfPieces) {
	var iPieceCounter;

	for (iPieceCounter = 0; iPieceCounter < teamOfPieces.length; iPieceCounter++) {
		drawPiece(teamOfPieces[iPieceCounter]);
	}
}

function drawPieces() {
    if(nP === 2) {
        drawTeamOfPieces(json.red);
        drawTeamOfPieces(json.yellow);
    }
    else if(nP === 3) {
        drawTeamOfPieces(json.red);
        drawTeamOfPieces(json.blue);
        drawTeamOfPieces(json.orange);
    }
    else if(nP === 4) {
        drawTeamOfPieces(json.black);
        drawTeamOfPieces(json.blue);
        drawTeamOfPieces(json.green);
        drawTeamOfPieces(json.orange);
    }
    else {
        drawTeamOfPieces(json.red);
        drawTeamOfPieces(json.black);
        drawTeamOfPieces(json.blue);
        drawTeamOfPieces(json.yellow);
        drawTeamOfPieces(json.green);
        drawTeamOfPieces(json.orange);
    }
}

function drawRow(iRowCounter) {
    var iBlockCounter;

    if(iRowCounter === 0) {
        drawBlock(6, iRowCounter);
    }
    else if(iRowCounter === 1) {
        drawBlock(5, iRowCounter);
        drawBlock(6, iRowCounter);
    }
    else if(iRowCounter === 2) {
        drawBlock(5, iRowCounter);
        drawBlock(6, iRowCounter);
        drawBlock(7, iRowCounter);
    }
    else if(iRowCounter === 3) {
        drawBlock(4, iRowCounter);
        drawBlock(5, iRowCounter);
        drawBlock(6, iRowCounter);
        drawBlock(7, iRowCounter);
    }
    else if(iRowCounter === 4) {
        for (iBlockCounter = 0; iBlockCounter < NUMBER_OF_COLS; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 5) {
        for (iBlockCounter = 0; iBlockCounter < NUMBER_OF_COLS - 1; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 6) {
        for (iBlockCounter = 1; iBlockCounter < NUMBER_OF_COLS - 1; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 7) {
        for (iBlockCounter = 1; iBlockCounter < NUMBER_OF_COLS - 2; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 8) {
        for (iBlockCounter = 2; iBlockCounter < NUMBER_OF_COLS - 2; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 9) {
        for (iBlockCounter = 1; iBlockCounter < NUMBER_OF_COLS - 2; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 10) {
        for (iBlockCounter = 1; iBlockCounter < NUMBER_OF_COLS - 1; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 12) {
        for (iBlockCounter = 0; iBlockCounter < NUMBER_OF_COLS; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 11) {
        for (iBlockCounter = 0; iBlockCounter < NUMBER_OF_COLS - 1; iBlockCounter++) {
            drawBlock(iBlockCounter, iRowCounter);
        }
    }
    else if(iRowCounter === 16) {
        drawBlock(6, iRowCounter);
    }
    else if(iRowCounter === 15) {
        drawBlock(5, iRowCounter);
        drawBlock(6, iRowCounter);
    }
    else if(iRowCounter === 14) {
        drawBlock(5, iRowCounter);
        drawBlock(6, iRowCounter);
        drawBlock(7, iRowCounter);
    }
    else if(iRowCounter === 13) {
        drawBlock(4, iRowCounter);
        drawBlock(5, iRowCounter);
        drawBlock(6, iRowCounter);
        drawBlock(7, iRowCounter);
    }

}

function drawBoard() {
	var iRowCounter;

	for (iRowCounter = 0; iRowCounter < NUMBER_OF_ROWS; iRowCounter++) {
		drawRow(iRowCounter);
	}

}

function checkIfPieceClicked(clickedBlock) {
    var pieceAtBlock = getPieceAtBlockForTeam(currentTurn, clickedBlock);

    if(currentTurn)
    
    if(pieceAtBlock !== null) {
        selectedPiece = pieceAtBlock;
    }
}

function drawNewPawn() {
    var iRowCounter = currentTurn[selectedPiece.position].col,
        iBlockCounter = currentTurn[selectedPiece.position].row;

    if(currentTurn === json.red) {
        ctx.fillStyle = RED;
    }
    else if(currentTurn === json.blue) {
        ctx.fillStyle = BLUE;
    }
    else if(currentTurn === json.yellow) {
        ctx.fillStyle = YELLOW;
    }
    else if(currentTurn === json.green) {
        ctx.fillStyle = GREEN;
    }
    else if(currentTurn === json.black) {
        ctx.fillStyle = BLACK;
    }
    else if(currentTurn === json.orange) {
        ctx.fillStyle = ORANGE;
    }

    if(iBlockCounter % 2) {
        ctx.beginPath();
        ctx.arc(iRowCounter * BLOCK_SIZE + BLOCK_SIZE, iBlockCounter * BLOCK_SIZE + BLOCK_SIZE / 2,
            15, 0, 2 * Math.PI, false);
        ctx.fill();
        ctx.stroke();
    }
    else {
        ctx.beginPath();
        ctx.arc(iRowCounter * BLOCK_SIZE + BLOCK_SIZE / 2, iBlockCounter * BLOCK_SIZE + BLOCK_SIZE / 2,
            15, 0, 2 * Math.PI, false);
        ctx.fill();
        ctx.stroke();
    }
}

function getTurn(newTurn) {
    if(newTurn === "red") {
        return json.red;
    }
    else if(newTurn === "green") {
        return json.green;
    }
    else if(newTurn === "blue") {
        return json.blue;
    }
    else if(newTurn === "black") {
        return json.black;
    }
    else if(newTurn === "yellow") {
        return json.yellow;
    }
    else if(newTurn === "orange") {
        return json.orange;
    }
}

function getColor(colorText) {
    if(colorText === "green") {
        return GREEN;
    }
    else if(colorText === "red") {
        return RED;
    }
    else if(colorText === "yellow") {
        return YELLOW;
    }
    else if(colorText === "blue") {
        return BLUE;
    }
    else if(colorText === "black") {
        return BLACK;
    }
    else if(colorText === "orange") {
        return ORANGE;
    }
}

function movePiece(clickedBlock) {
    drawBlock(currentTurn[selectedPiece.position].col, currentTurn[selectedPiece.position].row);

    var team = currentTurn,
        teamFields = json.fields;
    var newPiece = getPieceAtBlockForTeam(json.fields, clickedBlock);

	team[selectedPiece.position].col = clickedBlock.col;
    team[selectedPiece.position].row = clickedBlock.row;
    team[selectedPiece.position].sRow = teamFields[newPiece.position].sRow;
    team[selectedPiece.position].sCol = teamFields[newPiece.position].sCol;

	drawNewPawn();

	selectedPiece = null;
}

function checkPiece(clickedBlock) {
    var pieceAtBlock = getPieceAtBlockForTeam(json.fields, clickedBlock);

    if(pieceAtBlock !== null) {
        return true;
    }
    else {
        return false;
    }
}

function processMove(clickedBlock) {
    var pieceAtBlock = getPieceAtBlockForTeam(currentTurn, clickedBlock);
    var correctPiece = checkPiece(clickedBlock);
    
    if(pieceAtBlock !== null) {
        checkIfPieceClicked(clickedBlock);
    }
	else if (correctPiece) {
		var team = currentTurn,
            teamFields = json.fields;
        var newPiece = getPieceAtBlockForTeam(json.fields, clickedBlock);

        sendMessage(currentTurn[selectedPiece.position].sRow, currentTurn[selectedPiece.position].sCol, 
            teamFields[newPiece.position].sRow, teamFields[newPiece.position].sCol);
	}
}

function board_click(ev, rect) {
	var x = ev.clientX - rect.left,
		y = ev.clientY - rect.top,
        clickedBlock = screenToBlock(x, y);

	if (selectedPiece === null) {
        checkIfPieceClicked(clickedBlock);
	} else {
		processMove(clickedBlock);
	}
}

/* first method - onload in html */
function draw() {
	canvas = document.getElementById('chess');

	if (canvas.getContext) {
        ctx = canvas.getContext('2d');
        
        BLOCK_SIZE = canvas.height / NUMBER_OF_ROWS;

		drawBoard(); //draws board without pawns

		defaultPositions(); //creates json

		canvas.addEventListener('click', function (ev) {
            var rect = canvas.getBoundingClientRect();
            board_click(ev, rect);
        }, false);
	} else {
		alert("Canvas not supported!");
	}
}

/* row and col - on canvas
    sRow and sCol for server */
function defaultPositions() {
	json = {
        "fields":
            [
                {
					"row": 0,
                    "col": 6,
                    "sRow": 1,
                    "sCol": 5,
                },
                {
					"row": 1,
                    "col": 5,
                    "sRow": 2,
                    "sCol": 5,
                },
                {
					"row": 1,
                    "col": 6,
                    "sRow": 2,
                    "sCol": 6,
                },
                {
					"row": 2,
                    "col": 5,
                    "sRow": 3,
                    "sCol": 5,
                },
                {
					"row": 2,
                    "col": 6,
                    "sRow": 3,
                    "sCol": 6,
                },
                {
					"row": 2,
                    "col": 7,
                    "sRow": 3,
                    "sCol": 7,
                },
                {
					"row": 3,
                    "col": 4,
                    "sRow": 4,
                    "sCol": 5,
                },
                {
					"row": 3,
                    "col": 5,
                    "sRow": 4,
                    "sCol": 6,
                },
                {
					"row": 3,
                    "col": 6,
                    "sRow": 4,
                    "sCol": 7,
                },
                {
					"row": 3,
                    "col": 7,
                    "sRow": 4,
                    "sCol": 8,
                },
                {
					"row": 4,
                    "col": 0,
                    "sRow": 5,
                    "sCol": 1,
                },
                {
					"row": 4,
                    "col": 1,
                    "sRow": 5,
                    "sCol": 2,
                },
                {
					"row": 4,
                    "col": 2,
                    "sRow": 5,
                    "sCol": 3,
                },
                {
					"row": 4,
                    "col": 3,
                    "sRow": 5,
                    "sCol": 4,
                },
                {
					"row": 4,
                    "col": 4,
                    "sRow": 5,
                    "sCol": 5,
                },
                {
					"row": 4,
                    "col": 5,
                    "sRow": 5,
                    "sCol": 6,
                },
                {
					"row": 4,
                    "col": 6,
                    "sRow": 5,
                    "sCol": 7,
                },
                {
					"row": 4,
                    "col": 7,
                    "sRow": 5,
                    "sCol": 8,
                },
                {
					"row": 4,
                    "col": 8,
                    "sRow": 5,
                    "sCol": 9,
                },
                {
					"row": 4,
                    "col": 9,
                    "sRow": 5,
                    "sCol": 10,
                },
                {
					"row": 4,
                    "col": 10,
                    "sRow": 5,
                    "sCol": 11,
                },
                {
					"row": 4,
                    "col": 11,
                    "sRow": 5,
                    "sCol": 12,
                },
                {
					"row": 4,
                    "col": 12,
                    "sRow": 5,
                    "sCol": 13,
                },
                {
					"row": 5,
                    "col": 0,
                    "sRow": 6,
                    "sCol": 2,
                },
                {
					"row": 5,
                    "col": 1,
                    "sRow": 6,
                    "sCol": 3,
                },
                {
					"row": 5,
                    "col": 2,
                    "sRow": 6,
                    "sCol": 4,
                },
                {
					"row": 5,
                    "col": 3,
                    "sRow": 6,
                    "sCol": 5,
                },
                {
					"row": 5,
                    "col": 4,
                    "sRow": 6,
                    "sCol": 6,
                },
                {
					"row": 5,
                    "col": 5,
                    "sRow": 6,
                    "sCol": 7,
                },
                {
					"row": 5,
                    "col": 6,
                    "sRow": 6,
                    "sCol": 8,
                },
                {
					"row": 5,
                    "col": 7,
                    "sRow": 6,
                    "sCol": 9,
                },
                {
					"row": 5,
                    "col": 8,
                    "sRow": 6,
                    "sCol": 10,
                },
                {
					"row": 5,
                    "col": 9,
                    "sRow": 6,
                    "sCol": 11,
                },
                {
					"row": 5,
                    "col": 10,
                    "sRow": 6,
                    "sCol": 12,
                },
                {
					"row": 5,
                    "col": 11,
                    "sRow": 6,
                    "sCol": 13,
                },
                {
					"row": 6,
                    "col": 1,
                    "sRow": 7,
                    "sCol": 3,
                },
                {
                    "row": 6,
                    "col": 2,
                    "sRow": 7,
                    "sCol": 4,
                },
                {
					"row": 6,
                    "col": 3,
                    "sRow": 7,
                    "sCol": 5,
                },
                {
					"row": 6,
                    "col": 4,
                    "sRow": 7,
                    "sCol": 6,
                },
                {
					"row": 6,
                    "col": 5,
                    "sRow": 7,
                    "sCol": 7,
                },
                {
					"row": 6,
                    "col": 6,
                    "sRow": 7,
                    "sCol": 8,
                },
                {
					"row": 6,
                    "col": 7,
                    "sRow": 7,
                    "sCol": 9,
                },
                {
					"row": 6,
                    "col": 8,
                    "sRow": 7,
                    "sCol": 10,
                },
                {
					"row": 6,
                    "col": 9,
                    "sRow": 7,
                    "sCol": 11,
                },
                {
					"row": 6,
                    "col": 10,
                    "sRow": 7,
                    "sCol": 12,
                },
                {
					"row": 6,
                    "col": 11,
                    "sRow": 7,
                    "sCol": 13,
                },
                {
					"row": 7,
                    "col": 1,
                    "sRow": 8,
                    "sCol": 4,
                },
                {
                    "row": 7,
                    "col": 2,
                    "sRow": 8,
                    "sCol": 5,
                },
                {
					"row": 7,
                    "col": 3,
                    "sRow": 8,
                    "sCol": 6,
                },
                {
					"row": 7,
                    "col": 4,
                    "sRow": 8,
                    "sCol": 7,
                },
                {
					"row": 7,
                    "col": 5,
                    "sRow": 8,
                    "sCol": 8,
                },
                {
					"row": 7,
                    "col": 6,
                    "sRow": 8,
                    "sCol": 9,
                },
                {
					"row": 7,
                    "col": 7,
                    "sRow": 8,
                    "sCol": 10,
                },
                {
					"row": 7,
                    "col": 8,
                    "sRow": 8,
                    "sCol": 11,
                },
                {
					"row": 7,
                    "col": 9,
                    "sRow": 8,
                    "sCol": 12,
                },
                {
					"row": 7,
                    "col": 10,
                    "sRow": 8,
                    "sCol": 13,
                },
                {
					"row": 8,
                    "col": 2,
                    "sRow": 9,
                    "sCol": 5,
                },
                {
					"row": 8,
                    "col": 3,
                    "sRow": 9,
                    "sCol": 6,
                },
                {
					"row": 8,
                    "col": 4,
                    "sRow": 9,
                    "sCol": 7,
                },
                {
					"row": 8,
                    "col": 5,
                    "sRow": 9,
                    "sCol": 8,
                },
                {
					"row": 8,
                    "col": 6,
                    "sRow": 9,
                    "sCol": 9,
                },
                {
					"row": 8,
                    "col": 7,
                    "sRow": 9,
                    "sCol": 10,
                },
                {
					"row": 8,
                    "col": 8,
                    "sRow": 9,
                    "sCol": 11,
                },
                {
					"row": 8,
                    "col": 9,
                    "sRow": 9,
                    "sCol": 12,
                },
                {
					"row": 8,
                    "col": 10,
                    "sRow": 9,
                    "sCol": 13,
                },
                {
					"row": 9,
                    "col": 1,
                    "sRow": 10,
                    "sCol": 5,
                },
                {
					"row": 9,
                    "col": 2,
                    "sRow": 10,
                    "sCol": 6,
                },
                {
					"row": 9,
                    "col": 3,
                    "sRow": 10,
                    "sCol": 7,
                },
                {
					"row": 9,
                    "col": 4,
                    "sRow": 10,
                    "sCol": 8,
                },
                {
					"row": 9,
                    "col": 5,
                    "sRow": 10,
                    "sCol": 9,
                },
                {
					"row": 9,
                    "col": 6,
                    "sRow": 10,
                    "sCol": 10,
                },
                {
					"row": 9,
                    "col": 7,
                    "sRow": 10,
                    "sCol": 11,
                },
                {
					"row": 9,
                    "col": 8,
                    "sRow": 10,
                    "sCol": 12,
                },
                {
					"row": 9,
                    "col": 9,
                    "sRow": 10,
                    "sCol": 13,
                },
                {
					"row": 9,
                    "col": 10,
                    "sRow": 10,
                    "sCol": 14,
                },
                {
					"row": 10,
                    "col": 1,
                    "sRow": 11,
                    "sCol": 5,
                },
                {
					"row": 10,
                    "col": 2,
                    "sRow": 11,
                    "sCol": 6,
                },
                {
					"row": 10,
                    "col": 3,
                    "sRow": 11,
                    "sCol": 7,
                },
                {
					"row": 10,
                    "col": 4,
                    "sRow": 11,
                    "sCol": 8,
                },
                {
					"row": 10,
                    "col": 5,
                    "sRow": 11,
                    "sCol": 9,
                },
                {
					"row": 10,
                    "col": 6,
                    "sRow": 11,
                    "sCol": 10,
                },
                {
					"row": 10,
                    "col": 7,
                    "sRow": 11,
                    "sCol": 11,
                },
                {
					"row": 10,
                    "col": 8,
                    "sRow": 11,
                    "sCol": 12,
                },
                {
					"row": 10,
                    "col": 9,
                    "sRow": 11,
                    "sCol": 13,
                },
                {
					"row": 10,
                    "col": 10,
                    "sRow": 11,
                    "sCol": 14,
                },
                {
					"row": 10,
                    "col": 11,
                    "sRow": 11,
                    "sCol": 15,
                },
                {
					"row": 11,
                    "col": 0,
                    "sRow": 12,
                    "sCol": 5,
                },
                {
					"row": 11,
                    "col": 1,
                    "sRow": 12,
                    "sCol": 6,
                },
                {
					"row": 11,
                    "col": 2,
                    "sRow": 12,
                    "sCol": 7,
                },
                {
					"row": 11,
                    "col": 3,
                    "sRow": 12,
                    "sCol": 8,
                },
                {
					"row": 11,
                    "col": 4,
                    "sRow": 12,
                    "sCol": 9,
                },
                {
					"row": 11,
                    "col": 5,
                    "sRow": 12,
                    "sCol": 10,
                },
                {
					"row": 11,
                    "col": 6,
                    "sRow": 12,
                    "sCol": 11,
                },
                {
					"row": 11,
                    "col": 7,
                    "sRow": 12,
                    "sCol": 12,
                },
                {
					"row": 11,
                    "col": 8,
                    "sRow": 12,
                    "sCol": 13,
                },
                {
					"row": 11,
                    "col": 9,
                    "sRow": 12,
                    "sCol": 14,
                },
                {
					"row": 11,
                    "col": 10,
                    "sRow": 12,
                    "sCol": 15,
                },
                {
					"row": 11,
                    "col": 11,
                    "sRow": 12,
                    "sCol": 16,
                },
                {
					"row": 12,
                    "col": 0,
                    "sRow": 13,
                    "sCol": 5,
                },
                {
					"row": 12,
                    "col": 1,
                    "sRow": 13,
                    "sCol": 6,
                },
                {
					"row": 12,
                    "col": 2,
                    "sRow": 13,
                    "sCol": 7,
                },
                {
					"row": 12,
                    "col": 3,
                    "sRow": 13,
                    "sCol": 8,
                },
                {
					"row": 12,
                    "col": 4,
                    "sRow": 13,
                    "sCol": 9,
                },
                {
					"row": 12,
                    "col": 5,
                    "sRow": 13,
                    "sCol": 10,
                },
                {
					"row": 12,
                    "col": 6,
                    "sRow": 13,
                    "sCol": 11,
                },
                {
					"row": 12,
                    "col": 7,
                    "sRow": 13,
                    "sCol": 12,
                },
                {
					"row": 12,
                    "col": 8,
                    "sRow": 13,
                    "sCol": 13,
                },
                {
					"row": 12,
                    "col": 9,
                    "sRow": 13,
                    "sCol": 14,
                },
                {
					"row": 12,
                    "col": 10,
                    "sRow": 13,
                    "sCol": 15,
                },
                {
					"row": 12,
                    "col": 11,
                    "sRow": 13,
                    "sCol": 16,
                },
                {
					"row": 12,
                    "col": 12,
                    "sRow": 13,
                    "sCol": 17,
                },
                {
					"row": 13,
                    "col": 4,
                    "sRow": 14,
                    "sCol": 10,
                },
                {
					"row": 13,
                    "col": 5,
                    "sRow": 14,
                    "sCol": 11,
                },
                {
					"row": 13,
                    "col": 6,
                    "sRow": 14,
                    "sCol": 12,
                },
                {
					"row": 13,
                    "col": 7,
                    "sRow": 14,
                    "sCol": 13,
                },
                {
					"row": 14,
                    "col": 5,
                    "sRow": 15,
                    "sCol": 11,
                },
                {
					"row": 14,
                    "col": 6,
                    "sRow": 15,
                    "sCol": 12,
                },
                {
					"row": 14,
                    "col": 7,
                    "sRow": 15,
                    "sCol": 13,
                },
                {
					"row": 15,
                    "col": 5,
                    "sRow": 16,
                    "sCol": 12,
                },
                {
					"row": 15,
                    "col": 6,
                    "sRow": 16,
                    "sCol": 13,
                },
                {
					"row": 16,
                    "col": 6,
                    "sRow": 17,
                    "sCol": 13,
				},
            ],
		"red":
			[
				{
					"row": 0,
                    "col": 6,
                    "sRow": 1,
                    "sCol": 5,
                },
                {
					"row": 1,
                    "col": 5,
                    "sRow": 2,
                    "sCol": 5,
                },
                {
					"row": 1,
                    "col": 6,
                    "sRow": 2,
                    "sCol": 6,
                },
                {
					"row": 2,
                    "col": 5,
                    "sRow": 3,
                    "sCol": 5,
                },
                {
					"row": 2,
                    "col": 6,
                    "sRow": 3,
                    "sCol": 6,
                },
                {
					"row": 2,
                    "col": 7,
                    "sRow": 3,
                    "sCol": 7,
                },
                {
					"row": 3,
                    "col": 4,
                    "sRow": 4,
                    "sCol": 5,
                },
                {
					"row": 3,
                    "col": 5,
                    "sRow": 4,
                    "sCol": 6,
                },
                {
					"row": 3,
                    "col": 6,
                    "sRow": 4,
                    "sCol": 7,
                },
                {
					"row": 3,
                    "col": 7,
                    "sRow": 4,
                    "sCol": 8,
				},
            ],
        "black":
			[
				{
					"row": 4,
                    "col": 0,
                    "sRow": 5,
                    "sCol": 1,
                },
                {
					"row": 4,
                    "col": 1,
                    "sRow": 5,
                    "sCol": 2,
                },
                {
					"row": 4,
                    "col": 2,
                    "sRow": 5,
                    "sCol": 3,
                },
                {
					"row": 4,
                    "col": 3,
                    "sRow": 5,
                    "sCol": 4,
                },
                {
					"row": 5,
                    "col": 0,
                    "sRow": 6,
                    "sCol": 2,
                },
                {
					"row": 5,
                    "col": 1,
                    "sRow": 6,
                    "sCol": 3,
                },
                {
					"row": 5,
                    "col": 2,
                    "sRow": 6,
                    "sCol": 4,
                },
                {
					"row": 6,
                    "col": 1,
                    "sRow": 7,
                    "sCol": 3,
                },
                {
                    "row": 6,
                    "col": 2,
                    "sRow": 7,
                    "sCol": 4,
                },
                {
					"row": 7,
                    "col": 1,
                    "sRow": 8,
                    "sCol": 4,
				},
            ],
        "orange":
			[
				{
					"row": 9,
                    "col": 1,
                    "sRow": 10,
                    "sCol": 5,
                },
                {
					"row": 10,
                    "col": 1,
                    "sRow": 11,
                    "sCol": 5,
                },
                {
					"row": 10,
                    "col": 2,
                    "sRow": 11,
                    "sCol": 6,
                },
                {
					"row": 11,
                    "col": 0,
                    "sRow": 12,
                    "sCol": 5,
                },
                {
					"row": 11,
                    "col": 1,
                    "sRow": 12,
                    "sCol": 6,
                },
                {
					"row": 11,
                    "col": 2,
                    "sRow": 12,
                    "sCol": 7,
                },
                {
					"row": 12,
                    "col": 0,
                    "sRow": 13,
                    "sCol": 5,
                },
                {
					"row": 12,
                    "col": 1,
                    "sRow": 13,
                    "sCol": 6,
                },
                {
					"row": 12,
                    "col": 2,
                    "sRow": 13,
                    "sCol": 7,
                },
                {
					"row": 12,
                    "col": 3,
                    "sRow": 13,
                    "sCol": 8,
				},
            ],
        "green":
			[
				{
					"row": 4,
                    "col": 9,
                    "sRow": 5,
                    "sCol": 10,
                },
                {
					"row": 4,
                    "col": 10,
                    "sRow": 5,
                    "sCol": 11,
                },
                {
					"row": 4,
                    "col": 11,
                    "sRow": 5,
                    "sCol": 12,
                },
                {
					"row": 4,
                    "col": 12,
                    "sRow": 5,
                    "sCol": 13,
                },
                {
					"row": 5,
                    "col": 9,
                    "sRow": 6,
                    "sCol": 11,
                },
                {
					"row": 5,
                    "col": 10,
                    "sRow": 6,
                    "sCol": 12,
                },
                {
					"row": 5,
                    "col": 11,
                    "sRow": 6,
                    "sCol": 13,
                },
                {
					"row": 6,
                    "col": 10,
                    "sRow": 7,
                    "sCol": 12,
                },
                {
					"row": 6,
                    "col": 11,
                    "sRow": 7,
                    "sCol": 13,
                },
                {
					"row": 7,
                    "col": 10,
                    "sRow": 8,
                    "sCol": 13,
				},
            ],
        "blue":
			[
				{
					"row": 9,
                    "col": 10,
                    "sRow": 10,
                    "sCol": 14,
                },
                {
					"row": 10,
                    "col": 10,
                    "sRow": 11,
                    "sCol": 14,
                },
                {
					"row": 10,
                    "col": 11,
                    "sRow": 11,
                    "sCol": 15,
                },
                {
					"row": 11,
                    "col": 9,
                    "sRow": 12,
                    "sCol": 14,
                },
                {
					"row": 11,
                    "col": 10,
                    "sRow": 12,
                    "sCol": 15,
                },
                {
					"row": 11,
                    "col": 11,
                    "sRow": 12,
                    "sCol": 16,
                },
                {
					"row": 12,
                    "col": 9,
                    "sRow": 13,
                    "sCol": 14,
                },
                {
					"row": 12,
                    "col": 10,
                    "sRow": 13,
                    "sCol": 15,
                },
                {
					"row": 12,
                    "col": 11,
                    "sRow": 13,
                    "sCol": 16,
                },
                {
					"row": 12,
                    "col": 12,
                    "sRow": 13,
                    "sCol": 17,
				},
            ],
        "yellow":
			[
				{
					"row": 13,
                    "col": 4,
                    "sRow": 14,
                    "sCol": 10,
                },
                {
					"row": 13,
                    "col": 5,
                    "sRow": 14,
                    "sCol": 11,
                },
                {
					"row": 13,
                    "col": 6,
                    "sRow": 14,
                    "sCol": 12,
                },
                {
					"row": 13,
                    "col": 7,
                    "sRow": 14,
                    "sCol": 13,
                },
                {
					"row": 14,
                    "col": 5,
                    "sRow": 15,
                    "sCol": 11,
                },
                {
					"row": 14,
                    "col": 6,
                    "sRow": 15,
                    "sCol": 12,
                },
                {
					"row": 14,
                    "col": 7,
                    "sRow": 15,
                    "sCol": 13,
                },
                {
					"row": 15,
                    "col": 5,
                    "sRow": 16,
                    "sCol": 12,
                },
                {
					"row": 15,
                    "col": 6,
                    "sRow": 16,
                    "sCol": 13,
                },
                {
					"row": 16,
                    "col": 6,
                    "sRow": 17,
                    "sCol": 13,
				},
            ]
	};
}