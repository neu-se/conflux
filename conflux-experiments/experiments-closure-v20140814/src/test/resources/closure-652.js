/**
 * @fileoverview Polyfill and helper functions for Object.observe.
 *
 * This module should contain the polyfill code for Object.observe and helper functions.
 */
goog.provide('fs.observe');

goog.require('fs.debug');

/**
 * Observe changes in the given object using the given listener.
 * @param {Object}                   obj      The object to observe.
 * @param {function(Array.<Object>)} listener The listener to register.
 * @param {Array.<string>}           accept   The changes to pass to the listener ("add", "update",
 *                                              "delete", "reconfigure", "setPrototype", "preventExtensions")
 * @return {boolean} Whether the object was successfully hooked.
 */
fs.observe.listen = function(obj, listener) {
  try {
    if (obj.observe === undefined) {
      // TODO: polyfill
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