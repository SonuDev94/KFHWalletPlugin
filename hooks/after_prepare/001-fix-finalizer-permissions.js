const fs = require('fs');
const path = require('path');

module.exports = function(ctx) {
    const projectRoot = ctx.opts.projectRoot;
    const finalizerPath = path.join(projectRoot, 'platforms', 'android', 'app', 'libs', 'finalizer');
    
    if (fs.existsSync(finalizerPath)) {
        fs.chmodSync(finalizerPath, 0o755);  // rwxr-xr-x
        console.log('✅ FIXED: finalizer permissions set to executable');
    } else {
        console.warn('⚠️ finalizer not found:', finalizerPath);
    }
};