package net.thevpc.nsite.processor.base;

enum TagTokenType {
    PLAIN,
    EXPR,
    IF,
    CTRL_ELSE_IF,
    CTRL_ELSE,
    CTRL_END,
    INCLUDE,
    FOR,
    STATEMENT,
    CTRL_OTHER,
}
