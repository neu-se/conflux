function tfGenerator() {
  var gv = 0;
  try {
    gv = 1;
    yield gv;
  } finally {
    gv = 2;
    yield gv;
  }
  gv = 90;
  yield gv;
}