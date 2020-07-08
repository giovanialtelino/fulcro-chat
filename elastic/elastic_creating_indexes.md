  
curl -X PUT "localhost:9200/chat?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "properties": {
      "full_name": {
        "type":  "text"
      }
    }
  }
}
'
