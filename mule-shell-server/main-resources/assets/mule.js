class MuleShellConsole {

    constructor(consoleDiv) {
        this.consoleDiv = consoleDiv;
        this.continuation = false;
        this.remainingCode = "";
        this.consoleLine = '<div class="line terminal"></div>';
    }

    static getPrompt(continuation) {
        const p1 = "$";
        const p2 = ">";
        return continuation === false ? p1 : p2;
    }

    scrollIntoView() {
        this.consoleDiv[0].lastElementChild.scrollIntoView();
    }

    addOutput(text) {
        $(this.consoleLine).text(text).appendTo(consoleDiv);
    }

    processExpressionResult(lastInput, expression, data) {
        console.log("Response: " + JSON.stringify(data));

        this.addOutput(MuleShellConsole.getPrompt(this.continuation) + " " + lastInput);

        if (data == null) {
            this.addOutput("Expression: " + JSON.stringify(expression));
            this.addOutput("Error: Returned null result.");
            this.continuation = false;
            this.remainingCode = "";
            this.scrollIntoView();
            return;
        }

        this.continuation = data.continuation;
        this.remainingCode = data.remainingCode;

        const snippetArray = data.output != null ? data.output : [];

        for (let i in snippetArray) {
            const snippet = snippetArray[i];

            if (snippet.type === null) {

            } else if (snippet.type === 'console') {
                this.processConsole(snippet);

            } else if (snippet.type === 'error') {
                this.processErrorSnippet(snippet);

            } else if (snippet.type === 'expression') {
                this.processExpressionSnippet(snippet);

            } else if (snippet.type === 'import') {
                this.processImportSnippet(snippet);

            } else if (snippet.type === 'method') {
                this.processMethodSnippet(snippet, snippetArray);

            } else if (snippet.type === 'statement') {
                this.processStatementSnippet(snippet);

            } else if (snippet.type === 'type') {
                this.processTypeSnippet(snippet, snippetArray);

            } else if (snippet.type === 'variable') {
                this.processVariableSnippet(snippet, snippetArray);

            } else {
                console.warn("Unhandled type: " + snippet.type);
            }
        }

        if (this.continuation === false) {
            this.addOutput('\n');
        }

        if (this.continuation === false || snippetArray.length === 0) {
            this.scrollIntoView();
        }
    }

    processConsole(snippet) {
        this.processDiagnostic(snippet.diagnostic);

        const messageArray = snippet.value.split('\n');
        for (let m in messageArray) {
            const message = messageArray[m];
            this.addOutput(message);
            this.scrollIntoView();
        }
    }

    processDiagnostic(diagnostic) {
        if (diagnostic.length > 0) {
            this.continuation = false;
        }

        for (let i in diagnostic) {
            const element = diagnostic[i];

            this.addOutput("|  Error:");

            // Slit the error message per line.
            const messageArray = element.message.split('\n');
            for (let m in messageArray) {
                const message = messageArray[m];
                // Ignore this line output when showing errors:
                if (message === "  location: class ") {
                    continue;
                }
                this.addOutput("|  " + message);
            }

            // Display the relevant line of code with indicator line.
            const input = element.input;
            const start = parseInt(element.start, 10);
            const end = parseInt(element.end, 10);
            const left = this.leadingCut(input, start - 1) + 1;
            const right = this.trailingCut(input, end);

            // JShell API may send end == start for "expected this here".
            const diagnosticLine = input.substr(left, left === right ? 1 : right - left);

            this.addOutput("|  " + diagnosticLine);

            if (start + 1 >= end) {
                const text = " ".repeat(start - left) + "^";
                this.addOutput("|  " + text);
            }
            if (start + 1 < end) {
                const pad = " ".repeat(start - left);
                const bar = "^" + (start + 2 < end ? "-".repeat(end - start - 2) : "") + "^";
                const text = pad + bar;
                this.addOutput("|  " + text);
            }
            mule.scrollIntoView();
        }
    }

    processExpressionSnippet(snippet) {
        this.processDiagnostic(snippet.diagnostic);

        if (snippet.status === 'valid') {
            const type = snippet.typeName;
            const name = snippet.name;
            const value = snippet.value;
            const text = "| " + type + " " + name + " = " + value;
            this.addOutput(text);
        }
    }

    processErrorSnippet(snippet) {
        this.processDiagnostic(snippet.diagnostic);
    }

    processImportSnippet(snippet) {
        this.processDiagnostic(snippet.diagnostic);
    }

    processMethodSnippet(snippet, snippetArray) {
        this.processDiagnostic(snippet.diagnostic);

        if (snippet.status === 'valid') {
            const name = snippet.name;
            let action = 'created:';
            for (let i in snippetArray) {
                const check = snippetArray[i];
                console.log(JSON.stringify(check));
                if (check.type === 'method' && check.name === name && check.status === 'overwritten') {
                    action = 'modified:';
                    break;
                }
            }

            const text = "| " + action + " method " + snippet.fullName;
            this.addOutput(text);
        }
    }

    processStatementSnippet(snippet) {
        this.processDiagnostic(snippet.diagnostic);
    }

    processTypeSnippet(snippet, snippetArray) {
        this.processDiagnostic(snippet.diagnostic);

        const predicate = function (x) {
            return x.type === snippet.type &&
                x.name === snippet.name &&
                x.status === 'overwritten';
        };

        if (snippet.status === 'valid') {
            const typeName = snippet.typeName;
            const check = this.findInArray(snippetArray, predicate);
            const action = check ? check.typeName === snippet.typeName ? 'modified:' : 'replaced:' : 'created:';
            const text = "| " + action + " " + typeName + " " + snippet.name;
            this.addOutput(text);
        }
    }

    processVariableSnippet(snippet, snippetArray) {
        this.processDiagnostic(snippet.diagnostic);

        const predicate = function (x) {
            return x.type === snippet.type &&
                x.name === snippet.name &&
                x.status === 'overwritten';
        };

        if (snippet.status === 'valid') {
            const typeName = snippet.typeName;
            const check = this.findInArray(snippetArray, predicate);
            const action = check ? check.typeName === snippet.typeName ? 'modified:' : 'replaced:' : 'created:';
            const value = snippet.value;
            const text = "| " + action + " " + typeName + " " + snippet.name + (value !== null ? " = " + value : "");
            this.addOutput(text)
        }
    }

    findInArray(array, predicate) {
        for (let i in array) {
            const check = array[i];
            if (predicate(check)) {
                return check;
            }
        }
        return null;
    }

    leadingCut(string, index) {
        return string.lastIndexOf('\n', index);
    }

    trailingCut(string, index) {
        const right = string.indexOf('\n', index);

        if (right < 0) {
            return string.length;
        }

        return right;
    }

    command(command) {
        let result = null;
        this.addOutput(MuleShellConsole.getPrompt(false) + " " + command);

        if (command.trim().startsWith("/help")) {
            this.commandDisplayHelp();
            result = true;
        } else {
            this.addOutput("|  Unknown command: " + command);
            result = false;
        }

        this.addOutput('\n');
        this.scrollIntoView();
        return result;
    }

    commandDisplayHelp() {
        this.addOutput("|  MuleShell is still in development.");
        this.addOutput("|  (This is not the JShell Tool)");
        this.addOutput("|");
        this.addOutput("|  This is not the JShell Tool ... ");
        this.addOutput("|  ... may you find using this tool familiar.");
        this.addOutput("|");
    }
}
