// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

#import "NEUISelectViewController.h"
#import <NEUIKit/NEUIBackNavigationController.h>
#import <NEUIKit/NEUICommon.h>
#import <NEUIKit/UIView+NEUIExtension.h>
#import "NEUIEmptyView.h"
#import "NEUIUserInfoCell.h"
#import "NEVoiceRoomLocalized.h"
#import "NTESGlobalMacro.h"

@interface NEUISelectViewController ()

@end

@implementation NEUISelectViewController
- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}
- (void)viewDidLoad {
  [super viewDidLoad];
  // Do any additional setup after loading the view.
  self.ne_UINavigationItem.navigationBarHidden = YES;
  [self didSetUpUI];
}
- (void)viewDidLayoutSubviews {
  [super viewDidLayoutSubviews];
  CGFloat top = [NEUICommon ne_statusBarHeight];

  _navBar.frame = CGRectMake(0, top, self.view.width, 40.0);
  [_navBar ne_cornerRadii:CGSizeMake(5, 5)
           addRectCorners:UIRectCornerTopLeft | UIRectCornerTopRight];
  _emptyView.frame =
      CGRectMake(0, _navBar.bottom, self.view.width, self.view.height - _navBar.bottom);
  _tableview.frame = _emptyView.frame;
}
- (void)didSetUpUI {
  [self.view addSubview:self.navBar];
  [self.view addSubview:self.tableview];
  [self.view addSubview:self.emptyView];
}
#pragma mark - Notciations
- (void)didMemberEnter:(NSNotification *)note {
  NEVoiceRoomMember *member = note.object;
  [self didMemberChanged:YES member:member];
}

- (void)didMemberLeave:(NSNotification *)note {
  NEVoiceRoomMember *member = note.object;
  [self didMemberChanged:NO member:member];
  [self deleteMemberWithUserId:member.account];
}
- (void)didMemberChanged:(BOOL)enter member:(NEVoiceRoomMember *)member {
}

- (void)deleteMemberWithUserId:(NSString *)userId {
  if (!userId) {
    return;
  }

  __block NSInteger index = -1;
  [self.showMembers enumerateObjectsUsingBlock:^(NEVoiceRoomMember *_Nonnull obj, NSUInteger idx,
                                                 BOOL *_Nonnull stop) {
    if ([userId isEqualToString:obj.account]) {
      index = idx;
      *stop = YES;
    }
  }];

  if (index >= 0) {
    [self.showMembers removeObjectAtIndex:index];
    [self.tableview reloadData];
  }
}
#pragma mark------------------------ UITableView datasource and delegate ------------------------
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return _showMembers.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  NEUIUserInfoCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell"];
  if (!cell) {
    cell = [[NEUIUserInfoCell alloc] initWithStyle:UITableViewCellStyleDefault
                                   reuseIdentifier:@"cell"];
  }
  NEVoiceRoomMember *member = _showMembers[indexPath.row];
  [cell refresh:member];
  return cell;
}
#pragma mark------------------------ Getter ------------------------
- (UITableView *)tableview {
  if (!_tableview) {
    _tableview = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)
                                              style:UITableViewStylePlain];
    _tableview.delegate = self;
    _tableview.dataSource = self;
    _tableview.rowHeight = 56.0;
    _tableview.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
    _tableview.backgroundView = nil;
    _tableview.backgroundColor = [UIColor whiteColor];
    _tableview.separatorStyle = UITableViewCellSeparatorStyleNone;
  }
  return _tableview;
}

- (NEVoiceRoomUINavigationBar *)navBar {
  if (!_navBar) {
    _navBar = [[NEVoiceRoomUINavigationBar alloc] init];
    __weak typeof(self) weakSelf = self;
    _navBar.backBlock = ^() {
      [weakSelf dismissViewControllerAnimated:YES completion:nil];
    };
    _navBar.arrowBackBlock = ^() {
      [weakSelf.navigationController popToRootViewControllerAnimated:YES];
    };
    _navBar.title = NELocalizedString(@"选择成员");
    _navBar.operationType = NEUIBarOperationTypeCancel;
  }
  return _navBar;
}

- (NEUIEmptyView *)emptyView {
  if (!_emptyView) {
    _emptyView = [[NEUIEmptyView alloc] initWithInfo:NELocalizedString(@"暂无群成员～")];
  }
  return _emptyView;
}

@end
