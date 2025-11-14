# -----------专辑检索DSL语句编写-----------

# 查询一：搜索框关键字匹配查询（标题 简介 主播）
POST /albuminfo/_search
{
  "query": {
    "bool": {
      :"should": [
        {
          "match": {
            "albumTitle": "美人"
          }
        },
        {
          "match": {
            "albumIntro": "美人"
          }
        },
        {
          "term": {
            "announcerName":{
              "value": "美人"
            }
          }
        }
      ]
    }
  }
}

# 查询二：一二三级分类过滤查询
POST albuminfo/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "term": {
            "category1Id": "2"
          }
        },
        {
          "term": {
            "category2Id": "105"
          }
        },
        {
          "term": {
            "category3Id": "1032"
          }
        }
      ]
    }
  }
}

# 查询三：一二三级分类过滤查询
# 错误演示：nested存储的数据，在查询时需要声明nested存储域
POST /albuminfo/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "term": {
            "attributeValueIndexList.attributedId": "1"
          }
        },
        {
          "term": {
            "attributeValueIndexList.valueId": "1"
          }
        }
      ]
    }
  }
}
# 正确演示
POST /albuminfo/_search
{
  "query": {
    "nested": {
      "path": "attributeValueIndexList",
      "query": {
        "bool": {
          "filter": [
            {
              "term": {
                "attributeValueIndexList.attributedId": "1"
              }
            },
            {
              "term": {
                "attributeValueIndexList.valueId": "1"
              }
            }
          ]
        }
      }
    }
  }
}

# 组合
POST /albuminfo/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "bool": {
            "should": [
              {
                "match": {
                  "albumTitle": "美人"
                }
              },
              {
                "match": {
                  "albumIntro": "美人"
                }
              },
              {
                "term": {
                  "announcerName": {
                    "value": "美人"
                  }
                }
              }
            ]
          }
        }
      ],
      "filter": [
        {
          "term": {
            "category1Id": "2"
          }
        },
        {
          "term": {
            "category2Id": "105"
          }
        },
        {
          "term": {
            "category3Id": "1032"
          }
        },
        {
          "nested": {
            "path": "attributeValueIndexList",
            "query": {
              "bool": {
                "filter": [
                  {
                    "term": {
                      "attributeValueIndexList.attributedId": "1"
                    }
                  },
                  {
                    "term": {
                      "attributeValueIndexList.valueId": "2"
                    }
                  }
                ]
              }
            }
          }
        }
      ]
    }
  }
}

# 查询四：分页查询
POST /albuminfo/_search
{
  "query": {
    "match_all": {}
  },
  "from": 0,
  "size": 10
}

# 查询五：高亮查询
# 前提：必须是关键字匹配查询
POST /albuminfo/_search
{
  "query": {
    "match": {
      "albumTitle": "美人"
    }
  },
  "highlight": {
    "pre_tags": ["<font color='red>"],
    "fields": {"albumTitle":{}},
    "post_tags": ["</font>"]
  }
}

# 查询六：排序查询
POST /albuminfo/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "hotScore": {
        "order": "desc"
      }
    }
  ]
}

POST /albuminfo/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "playStatNum": {
        "order": "desc"
      }
    }
  ]
}

POST /albuminfo/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "createTime": {
        "order": "desc"
      }
    }
  ]
}

# 查询七：结果过滤查询
POST /albuminfo/_search
{
  "query": {
    "match_all": {}
  },
  "_source": {
    "excludes": ["category1Id","category2Id","category3Id"]
  }
}


## 最终组合
POST /albuminfo/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "bool": {
            "should": [
              {
                "match": {
                  "albumTitle": "美人"
                }
              },
              {
                "match": {
                  "albumIntro": "美人"
                }
              },
              {
                "term": {
                  "announcerName": {
                    "value": "美人"
                  }
                }
              }
            ]
          }
        }
      ],
      "filter": [
        {
          "term": {
            "category1Id": "2"
          }
        },
        {
          "term": {
            "category2Id": "105"
          }
        },
        {
          "term": {
            "category3Id": "1032"
          }
        },
        {
          "nested": {
            "path": "attributeValueIndexList",
            "query": {
              "bool": {
                "filter": [
                  {
                    "term": {
                      "attributeValueIndexList.attributedId": "1"
                    }
                  },
                  {
                    "term": {
                      "attributeValueIndexList.valueId": "2"
                    }
                  }
                ]
              }
            }
          }
        }
      ]
    }
  },
  "from": 0,
  "size": 10,
  "highlight": {
    "pre_tags": [
      "<font color='red>"
    ],
    "fields": {
      "albumTitle": {}
    },
    "post_tags": [
      "</font>"
    ]
  },
  "sort": [
    {
      "createTime": {
        "order": "desc"
      }
    }
  ],
  "_source": {
    "excludes": [
      "category1Id",
      "category2Id",
      "category3Id"
    ]
  }
}
