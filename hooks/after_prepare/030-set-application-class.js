const fs = require('fs');
const path = require('path');

module.exports = function (context) {

    const projectRoot = context.opts.projectRoot;

    const manifestPath = path.join(
        projectRoot,
        'platforms',
        'android',
        'app',
        'src',
        'main',
        'AndroidManifest.xml'
    );

    if (!fs.existsSync(manifestPath)) {
        console.log('AndroidManifest.xml not found: ' + manifestPath);
        return;
    }

    let manifest = fs.readFileSync(manifestPath, 'utf8');

    const applicationRegex = /<application\b([^>]*)>/;

    if (!applicationRegex.test(manifest)) {
        throw new Error('Application tag not found in AndroidManifest.xml');
    }

    if (/android:name\s*=/.test(manifest.match(applicationRegex)[0])) {

        manifest = manifest.replace(
            /(<application\b[^>]*?)android:name\s*=\s*"[^"]*"/,
            '$1android:name="com.aub.mobilebanking.phone.eg.WaAppSystemContext"'
        );

    } else {

        manifest = manifest.replace(
            /<application\b/,
            '<application android:name="com.aub.mobilebanking.phone.eg.WaAppSystemContext"'
        );

    }

    fs.writeFileSync(manifestPath, manifest, 'utf8');

    console.log(
        'WaAppSystemContext added to AndroidManifest.xml'
    );
};