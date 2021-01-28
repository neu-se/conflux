goog.provide('fs.observe');

goog.require('fs.debug');

fs.observe.listen = function(obj, listener) {
  try {
    if (obj.observe === undefined) {
      // TODO
      throw;
    }
    obj.observe(listener);
    return true;
  }
  catch (e) {
    fs.debug.print('fs.observe failed with exception ' + e);
  }
  return false;
};