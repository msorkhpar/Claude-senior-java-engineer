The marketplace imports product lists that suppliers upload. Each supplier declares the charset
its file is written in when it signs up: some send UTF-8, a Danish bakery sends ISO-8859-1, and a
Dutch gift shop's till exports windows-1252. Since the import went live, the shop shows names
like `Sm�rrebr�d kit`, the gift cards' prices have lost their currency sign, and last week an
upload that broke off halfway through was imported anyway, with a garbled last line.

`names` already splits the text into product names. Finish the `TODO` in
`src/main/java/practice/FeedDecoder.java`:

- `decode(body, charsetName)`: the text of an upload's body, given the charset its supplier
  declared. A body that isn't valid text in that charset is refused with a
  `CharacterCodingException`, so the import skips it and asks the supplier to send it again.

About five lines.
