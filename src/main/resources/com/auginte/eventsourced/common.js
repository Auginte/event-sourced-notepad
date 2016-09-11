function htmlEncode(text) {
    var encodedText = text.replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\//g, '&#47;')
        .replace(/:/g, '&#58;')
        .replace(/\\/g, '&#92;');
    return encodedText;
}

function urlEncode(text) {
    return encodeURIComponent(text.replace(" ", "-"))
        .replace("'", "%27")
        .replace('"', "%22");
}