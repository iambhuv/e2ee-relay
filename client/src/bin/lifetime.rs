fn getaword<'a>() -> &'a str {
  let word;
  {
    let str: &'a str = "Hello World";
    word = &str
  }
  word
}


fn main() {
  let word = getaword();

  println!("Word = {}", word);
}