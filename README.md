这个app只是用来供测试和了解该游戏，还需项目方自己设计和开发前端。

游戏规则：
跟大家平常玩的大富翁差不多。
有100个城市，掷骰子（自动）产生步数，人物就可以向前走，走到有主人的城市需要交过路费，也可以买下这个城市。如果有过路费，必须交过过路费才能继续往前走。
用户可以针对自己的城市设置logo，img,url等，类似广告位，游戏结束后这些就永久是该用户的了。
没有监狱、机会那些。也没有走一圈自动发钱（如果需要发游戏币，可以考虑加）。

一些设定：
初始城市价格1EOS，每位玩家购买后，价格要上升125%。其中110%归上一位玩家，15%进入奖池。
当最后一次购买城市后，如果3天之内没有任何人再次购买城市，则游戏结束，此时运营方开始分发奖池。
奖池分3部分，40%由所有城市的最终主人按当前价格比例分，35%归最后一位买城市的玩家，25%归游戏运营团队。
玩家的所得（过路费，卖城市升值部分，奖池分红）记录在其游戏余额中，可以随时提现成EOS
所有逻辑均在合约中，完全去中心化。

示例：
玩家A开始玩游戏，走到城市1，并买下，此时花费1EOS。 
玩家B此时玩游戏，走到城市1，需交0.1EOS的过路费（不交不能往前走），可以买下该城市，此时售价1.25EOS，其中1.1EOS归玩家A，0.15EOS进入奖池

测试app玩法：
1.当前部署在麒麟测试网，需要先到麒麟测试网注册账号，领EOS(测试网可以免费领），购买CPU,NET,RAM。
2.将测试网上生成的私钥导入到测试app带的钱包（点钱包管理->新建钱包->导入私钥）。后续实际上线时可以集成Scatter就行了，就没这么麻烦了。
3.输入账号名，点开始，此时可以看到自己的各种状态，以及城市，奖池等
4.点前进、付租金、买当前城市、提款、设置logo等进行游戏。注意最好先“前进”再购买城市，因为目前游戏尚未完全展开，所以有的城市尚未在合约里生成数据，“前进”之后就可以生成数据。

可以修改的设定：
城市数量/名字，城市初始价格，租金比例，价格增长比例，奖池比例等。
合约里并不记录城市名字，只有一个号码，所以可以在前端配置任何城市名字进去。

运营：
可以考虑同时开展多个游戏，如全球大富翁，欧洲大富翁，美国大富翁，中国大富翁....
初始时游戏需要宣传时间，可能要自己玩一下，以免3天没有买卖直接结束。
